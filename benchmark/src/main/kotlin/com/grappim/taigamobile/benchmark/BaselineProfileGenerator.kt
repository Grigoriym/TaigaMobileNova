package com.grappim.taigamobile.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

private const val TARGET_PACKAGE = "com.grappim.taigamobile.fdroid"

/**
 * Login is mandatory in this app (no anonymous path), but the session persists across a full
 * `am force-stop` (docs/perf/profiling.md), and `BaselineProfileRule` never clears app data
 * between iterations or `@Test`s (only kills the process) — logging in once by hand before
 * running the generator is enough to land this journey on the post-login "Select Project" screen
 * rather than the login form. See docs/EMULATOR_TESTING.md for the on-device login recipe.
 *
 * Only a cold-start journey — docs/revisit.md #38's `VerifyClass` finding was startup-wide, not
 * tied to a specific post-login navigation, so there's no concrete second journey to add yet.
 */
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun coldStart() = baselineProfileRule.collect(packageName = TARGET_PACKAGE) {
        pressHome()
        startActivityAndWait()
    }
}
