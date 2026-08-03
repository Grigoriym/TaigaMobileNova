package com.grappim.taigamobile.feature.workitem.ui.delegates.customfields

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedCustomAttributes
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CheckboxItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.DateItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.DropdownItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.NumberItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.RichTextItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.TextItemState
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class WorkItemCustomFieldsDelegateImplTest {

    private lateinit var sut: WorkItemCustomFieldsDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository = FakeWorkItemRepository()
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()
    private val dateTimeUtils: DateTimeUtils = FakeDateTimeUtils()

    @BeforeTest
    fun setup() {
        workItemRepository.patchCustomAttributesResult = PatchedCustomAttributes(version = PATCHED_VERSION)
        sut = WorkItemCustomFieldsDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator,
            dateTimeUtils = dateTimeUtils
        )
    }

    private fun state() = sut.customFieldsState.value

    /**
     * The delegate hands [PatchDataGenerator.getAttributesPatchPayload] a map keyed by the field id,
     * and the generator nests it under `attributes_values`. Unwrapping it here is what lets every
     * `getCustomFieldValue` branch be asserted through the public API — the function itself is private.
     */
    @Suppress("UNCHECKED_CAST")
    private fun lastPatchedAttributes(): Map<String, Any?> =
        workItemRepository.patchCustomAttributesCalls.last().payload["attributes_values"] as Map<String, Any?>

    private fun textItem(id: Long, original: String = "original", current: String = original) = TextItemState(
        id = id,
        description = null,
        label = "label-$id",
        originalValue = original,
        currentValue = current
    )

    private suspend fun save(
        item: CustomFieldItemState,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: (() -> Unit)? = null,
        doOnError: (Throwable) -> Unit = { }
    ) = sut.handleCustomFieldSave(
        item = item,
        customAttributesVersion = ATTRIBUTES_VERSION,
        workItemId = WORK_ITEM_ID,
        doOnPreExecute = doOnPreExecute,
        doOnSuccess = doOnSuccess,
        doOnError = doOnError
    )

    // --- setInitialCustomFields ---

    @Test
    fun `setInitialCustomFields - stores the items and the version`() {
        val items = persistentListOf<CustomFieldItemState>(textItem(1L), textItem(2L))

        sut.setInitialCustomFields(items, version = 7L)

        assertContentEquals(items, state().customFieldStateItems)
        assertEquals(7L, state().customFieldsVersion)
    }

    // --- setIsCustomFieldsWidgetExpanded ---

    @Test
    fun `on setIsCustomFieldsWidgetExpanded should update field`() {
        assertFalse(state().isCustomFieldsWidgetExpanded)

        state().setIsCustomFieldsWidgetExpanded(true)

        assertTrue(state().isCustomFieldsWidgetExpanded)

        state().setIsCustomFieldsWidgetExpanded(false)

        assertFalse(state().isCustomFieldsWidgetExpanded)
    }

    // --- onCustomFieldEditToggle ---

    @Test
    fun `onCustomFieldEditToggle - adds the id when the item is not being edited`() {
        val item = textItem(1L)

        state().onCustomFieldEditToggle(item)

        assertEquals(setOf(1L), state().editingItemIds)
    }

    @Test
    fun `onCustomFieldEditToggle - removes the id when the item is already being edited`() {
        val item = textItem(1L)
        state().onCustomFieldEditToggle(item)

        state().onCustomFieldEditToggle(item)

        assertTrue(state().editingItemIds.isEmpty())
    }

    @Test
    fun `onCustomFieldEditToggle - leaves the other edited ids alone`() {
        state().onCustomFieldEditToggle(textItem(1L))
        state().onCustomFieldEditToggle(textItem(2L))

        state().onCustomFieldEditToggle(textItem(1L))

        assertEquals(setOf(2L), state().editingItemIds)
    }

    // --- onCustomFieldChange ---

    @Test
    fun `onCustomFieldChange - replaces only the matching item`() {
        sut.setInitialCustomFields(persistentListOf(textItem(1L), textItem(2L)), version = 1L)

        state().onCustomFieldChange(textItem(2L, original = "original", current = "edited"))

        val items = state().customFieldStateItems
        assertEquals("original", items[0].currentValue)
        assertEquals("edited", items[1].currentValue)
    }

    @Test
    fun `onCustomFieldChange - an unknown id changes nothing`() {
        sut.setInitialCustomFields(persistentListOf(textItem(1L)), version = 1L)

        state().onCustomFieldChange(textItem(99L, current = "edited"))

        assertEquals(1, state().customFieldStateItems.size)
        assertEquals("original", state().customFieldStateItems[0].currentValue)
    }

    // --- handleCustomFieldSave: success ---

    @Test
    fun `handleCustomFieldSave - success - patches with the given version, id and task type`() = runTest {
        val item = textItem(1L, current = "edited")
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)

        save(item)

        val call = workItemRepository.patchCustomAttributesCalls.single()
        assertEquals(ATTRIBUTES_VERSION, call.customAttributesVersion)
        assertEquals(WORK_ITEM_ID, call.workItemId)
        assertEquals(commonTaskType, call.commonTaskType)
    }

    @Test
    fun `handleCustomFieldSave - success - invokes doOnPreExecute and doOnSuccess`() = runTest {
        val item = textItem(1L, current = "edited")
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)
        var preExecuteCalled = false
        var successCalled = false

        save(item, doOnPreExecute = { preExecuteCalled = true }, doOnSuccess = { successCalled = true })

        assertTrue(preExecuteCalled)
        assertTrue(successCalled)
    }

    @Test
    fun `handleCustomFieldSave - success - null doOnPreExecute and doOnSuccess are optional`() = runTest {
        val item = textItem(1L, current = "edited")
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)

        save(item, doOnPreExecute = null, doOnSuccess = null)

        assertEquals(1, workItemRepository.patchCustomAttributesCalls.size)
    }

    @Test
    fun `handleCustomFieldSave - success - stores the new version and clears loading`() = runTest {
        val item = textItem(1L, current = "edited")
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)

        save(item)

        assertEquals(PATCHED_VERSION, state().customFieldsVersion)
        assertFalse(state().isCustomFieldsLoading)
    }

    @Test
    fun `handleCustomFieldSave - success - the saved item's edited value becomes its original`() = runTest {
        val item = textItem(1L, original = "original", current = "edited")
        sut.setInitialCustomFields(persistentListOf(item, textItem(2L)), version = 1L)

        save(item)

        val saved = state().customFieldStateItems[0]
        assertEquals("edited", saved.originalValue)
        assertEquals("edited", saved.currentValue)
        assertFalse(saved.isModified)
        assertEquals("original", state().customFieldStateItems[1].originalValue)
    }

    @Test
    fun `handleCustomFieldSave - success - closes the edit mode of an editable item`() = runTest {
        val item = RichTextItemState(
            id = 1L,
            description = null,
            label = "label",
            originalValue = "original",
            currentValue = "edited"
        )
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)
        state().onCustomFieldEditToggle(item)

        save(item)

        assertTrue(state().editingItemIds.isEmpty())
    }

    /**
     * Documents current behaviour rather than endorsing it: the success path calls
     * `onCustomFieldEditToggle` unconditionally, so saving an item that was *not* in edit mode adds
     * it to `editingItemIds` instead of removing it. Harmless in the UI — `CustomFieldsWidget` reads
     * the set only for `EditableItem`s — but the set grows forever. See docs/revisit.md.
     */
    @Test
    fun `handleCustomFieldSave - success - adds a non-editing item to editingItemIds`() = runTest {
        val item = textItem(1L, current = "edited")
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)

        save(item)

        assertEquals(setOf(1L), state().editingItemIds)
    }

    // --- handleCustomFieldSave: payload composition ---

    @Test
    fun `handleCustomFieldSave - payload - the saved item sends its current value`() = runTest {
        val item = textItem(1L, original = "original", current = "edited")
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)

        save(item)

        assertEquals("edited", lastPatchedAttributes()["1"])
    }

    @Test
    fun `handleCustomFieldSave - payload - another modified item sends its original value`() = runTest {
        val saved = textItem(1L, current = "edited")
        val otherEdited = textItem(2L, original = "untouched", current = "in progress")
        sut.setInitialCustomFields(persistentListOf(saved, otherEdited), version = 1L)

        save(saved)

        assertEquals("edited", lastPatchedAttributes()["1"])
        assertEquals("untouched", lastPatchedAttributes()["2"])
    }

    @Test
    fun `handleCustomFieldSave - payload - an unmodified item sends its current value`() = runTest {
        val saved = textItem(1L, current = "edited")
        val untouched = textItem(2L, original = "same")
        sut.setInitialCustomFields(persistentListOf(saved, untouched), version = 1L)

        save(saved)

        assertEquals("same", lastPatchedAttributes()["2"])
    }

    // --- handleCustomFieldSave: getCustomFieldValue per item type ---

    @Test
    fun `handleCustomFieldSave - payload - a date is formatted through dateTimeUtils`() = runTest {
        val item = DateItemState(
            id = 1L,
            description = null,
            label = "date",
            originalValue = LocalDate(2024, 1, 15),
            currentValue = LocalDate(2025, 6, 30)
        )
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)

        save(item)

        assertEquals("2025-06-30", lastPatchedAttributes()["1"])
    }

    @Test
    fun `handleCustomFieldSave - payload - a cleared date is sent as null`() = runTest {
        val item = DateItemState(
            id = 1L,
            description = null,
            label = "date",
            originalValue = LocalDate(2024, 1, 15),
            currentValue = null
        )
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)

        save(item)

        assertNull(lastPatchedAttributes()["1"])
    }

    @Test
    fun `handleCustomFieldSave - payload - an unset date on another item is sent as null`() = runTest {
        val saved = textItem(1L, current = "edited")
        val dateWithoutOriginal = DateItemState(
            id = 2L,
            description = null,
            label = "date",
            originalValue = null,
            currentValue = LocalDate(2025, 6, 30)
        )
        sut.setInitialCustomFields(persistentListOf(saved, dateWithoutOriginal), version = 1L)

        save(saved)

        assertNull(lastPatchedAttributes()["2"])
    }

    @Test
    fun `handleCustomFieldSave - payload - a number is sent as a Long`() = runTest {
        val item = NumberItemState(
            id = 1L,
            description = null,
            label = "number",
            originalValue = "5",
            currentValue = "42"
        )
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)

        save(item)

        assertEquals(42L, lastPatchedAttributes()["1"])
    }

    @Test
    fun `handleCustomFieldSave - payload - an unparseable number is sent as null`() = runTest {
        val item = NumberItemState(
            id = 1L,
            description = null,
            label = "number",
            originalValue = "5",
            currentValue = "not a number"
        )
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)

        save(item)

        assertNull(lastPatchedAttributes()["1"])
    }

    @Test
    fun `handleCustomFieldSave - payload - checkbox and dropdown values pass through unchanged`() = runTest {
        val checkbox = CheckboxItemState(
            id = 1L,
            description = null,
            label = "checkbox",
            originalValue = false,
            currentValue = true
        )
        val dropdown = DropdownItemState(
            id = 2L,
            description = null,
            label = "dropdown",
            originalValue = "a",
            currentValue = "a",
            options = persistentListOf("a", "b")
        )
        sut.setInitialCustomFields(persistentListOf(checkbox, dropdown), version = 1L)

        save(checkbox)

        assertEquals(true, lastPatchedAttributes()["1"])
        assertEquals("a", lastPatchedAttributes()["2"])
    }

    // --- handleCustomFieldSave: failure ---

    @Test
    fun `handleCustomFieldSave - failure - invokes doOnError and clears loading`() = runTest {
        workItemRepository.patchCustomAttributesThrows = testException
        val item = textItem(1L, current = "edited")
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)
        var caught: Throwable? = null

        save(item, doOnError = { caught = it })

        assertEquals(testException.message, caught?.message)
        assertFalse(state().isCustomFieldsLoading)
    }

    @Test
    fun `handleCustomFieldSave - failure - keeps the version and does not save the item`() = runTest {
        workItemRepository.patchCustomAttributesThrows = testException
        val item = textItem(1L, original = "original", current = "edited")
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)

        save(item)

        assertEquals(1L, state().customFieldsVersion)
        assertEquals("original", state().customFieldStateItems[0].originalValue)
        assertTrue(state().editingItemIds.isEmpty())
    }

    @Test
    fun `handleCustomFieldSave - failure - doOnPreExecute ran but doOnSuccess did not`() = runTest {
        workItemRepository.patchCustomAttributesThrows = testException
        val item = textItem(1L, current = "edited")
        sut.setInitialCustomFields(persistentListOf(item), version = 1L)
        var preExecuteCalled = false
        var successCalled = false

        save(item, doOnPreExecute = { preExecuteCalled = true }, doOnSuccess = { successCalled = true })

        assertTrue(preExecuteCalled)
        assertFalse(successCalled)
    }

    private companion object {
        const val ATTRIBUTES_VERSION = 3L
        const val WORK_ITEM_ID = 111L
        const val PATCHED_VERSION = 4L
    }
}
