package com.grappim.taigamobile.di

import com.grappim.taigamobile.testing.MainDispatcherRule
import kotlinx.coroutines.test.StandardTestDispatcher
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.definition.BeanDefinition
import org.koin.core.error.NoDefinitionFoundException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Stands in for the compile-time dependency check the Koin IR/FIR plugin does not do
 * (`compileSafety = false`, see `KmpDiConventionPlugin`). Builds the real [KoinApp] graph and
 * resolves every registered definition, so a missing binding fails here instead of crashing the app
 * on launch.
 *
 * ## Why this lives in `jvmTest` and not `commonTest`
 *
 * The graph contains `expect/actual` modules (`PlatformComponentModule`, `PlatformStorageModule`,
 * `PlatformDBModule`) whose beans exist only per platform. JVM is a real supported target with a
 * complete set of actuals — desktop calls `startKoin<KoinApp>` for real — so the JVM graph is a whole
 * graph, checkable with no stubbing at all. Android and iOS actuals are **not** covered: their
 * modules differ (`androidContext`, Darwin engine, Room drivers), and verifying those needs an
 * instrumented or native test.
 *
 * ## What this catches
 *
 * A definition whose dependency has no binding — the `NoDefinitionFoundException` / "no definition
 * found" crash. Removing a `@Single`/`@Factory` from a class, a qualifier mismatch, or adding a
 * constructor parameter nothing provides all fail here.
 *
 * ## What this does NOT catch — read before trusting it
 *
 * - **Dropping a module from `AppModule.includes` is invisible on JVM.** `AppModule` also carries
 *   `@ComponentScan("com.grappim.taigamobile")`, and on JVM that scan reaches across module
 *   boundaries, so it re-discovers every bean the includes list would have contributed. Verified by
 *   deleting `UsersDataModule::class` from the list: the definition count and the result were
 *   unchanged. The includes list is what makes the graph work on **iOS Native**, where the
 *   cross-module scan finds nothing — and that is exactly the case this test cannot see.
 * - **Android and iOS graphs.** See above.
 * - **Nullable and lazy parameters.** A `T?` parameter resolves via `getOrNull()` and is silently
 *   null when unbound; a `Lazy<T>` resolves on first access, which never happens here. Neither
 *   raises, so neither fails this test.
 *
 * ## Why constructor failures are tolerated
 *
 * Koin resolves every constructor argument *before* invoking the constructor, so a definition whose
 * constructor body then throws has already proven its wiring. Only [NoDefinitionFoundException] is
 * treated as a failure; anything else is printed and tolerated. That is what makes the ViewModels
 * checkable at all — the 15 that take a route via `@InjectedParam` throw `DefinitionParameterException`
 * here, since nothing supplies a `parametersOf(route)` (that only exists at the Screen's
 * `koinViewModel { }` call site). That noise is expected.
 *
 * ## Why `Main` is replaced by a dispatcher that never runs
 *
 * Ten ViewModels start a `viewModelScope.launch` from their `init` block. On JVM
 * `kotlinx-coroutines-swing` is on the classpath, so `Dispatchers.Main` is a live thread and those
 * bodies really execute — querying an empty Room database and throwing, asynchronously, after this
 * test has moved on. `kotlinx-coroutines-test` registers `ExceptionCollectorAsService` as a
 * process-wide `CoroutineExceptionHandler` via `ServiceLoader`, which hands any such uncaught
 * exception to whichever `runTest` is active at that moment — failing an unrelated test chosen by
 * thread timing. `MainViewModelTest` was the usual victim.
 *
 * [StandardTestDispatcher] queues those launches and is never advanced, so they never run and
 * nothing escapes. Wiring is unaffected: it is proven when the constructor's arguments resolve,
 * before `init` would run.
 *
 * **This assumes every `viewModelScope.launch` in the codebase is undispatched** — true today
 * (no `launch(SomeDispatcher)` exists in `feature/` or `composeApp/`). An `init` that launches on
 * `Dispatchers.IO` would bypass this and bring the flake back. See
 * `docs/issues/2026-08-02-koingraphtest-leaks-coroutine-exceptions.md`.
 */
@OptIn(KoinInternalApi::class)
internal class KoinGraphTest {

    private val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    @Test
    fun `every definition in the graph resolves its dependencies`() {
        val koin = sharedTestKoinGraph
        val root = koin.scopeRegistry.rootScope

        // Snapshot the real graph before adding anything test-only, so the count below measures the
        // app and not the scaffolding. The registry maps every index key to a factory, so a
        // definition bound to secondary types (`@KoinViewModel` also binds `ViewModel`) appears more
        // than once; `distinct()` is identity here — InstanceFactory has no equals — which is
        // exactly the dedup wanted.
        val definitions = koin.instanceRegistry.instances.values
            .distinct()
            .map { it.beanDefinition }
            .filter { it.scopeQualifier == root.scopeQualifier }

        val missing = mutableListOf<String>()
        val constructionFailures = mutableListOf<String>()
        definitions.forEach { definition ->
            try {
                root.get<Any>(definition.primaryType, definition.qualifier, null)
            } catch (e: Throwable) {
                val cause = e.rootCause()
                if (cause is NoDefinitionFoundException) {
                    missing += "${definition.describe()}\n    ${cause.message}"
                } else {
                    constructionFailures +=
                        "${definition.describe()} -> ${cause::class.simpleName}: ${cause.message}"
                }
            }
        }

        println("KoinGraphTest: checked ${definitions.size} definitions")
        if (constructionFailures.isNotEmpty()) {
            println(
                "KoinGraphTest: ${constructionFailures.size} definition(s) resolved their " +
                    "dependencies but threw while constructing. Not a wiring failure:\n" +
                    constructionFailures.joinToString("\n")
            )
        }

        assertTrue(
            missing.isEmpty(),
            "${missing.size} definition(s) have a dependency with no binding:\n" +
                missing.joinToString("\n")
        )

        assertTrue(
            definitions.size >= MIN_EXPECTED_DEFINITIONS,
            "Only ${definitions.size} definitions were registered, expected at least " +
                "$MIN_EXPECTED_DEFINITIONS. Something stopped being registered. If the graph " +
                "legitimately shrank, lower the constant deliberately."
        )
    }

    private fun BeanDefinition<*>.describe(): String = buildString {
        append(primaryType.qualifiedName)
        qualifier?.let { append(" (qualifier: $it)") }
    }

    private tailrec fun Throwable.rootCause(): Throwable {
        val cause = cause
        return if (cause == null || cause === this) this else cause.rootCause()
    }

    private companion object {
        /**
         * Asserted last, so a genuine wiring break reports the missing bindings by name first. This
         * is the backstop for the case that report cannot see: a definition nothing else depends on
         * silently disappearing from the graph. Raise it when the graph grows; only lower it on a
         * deliberate removal.
         */
        const val MIN_EXPECTED_DEFINITIONS = 147
    }
}
