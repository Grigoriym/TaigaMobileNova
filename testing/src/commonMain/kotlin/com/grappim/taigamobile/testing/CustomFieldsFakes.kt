package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.TextItemState

fun getCustomFieldItemState(): CustomFieldItemState = TextItemState(
    id = getRandomLong(),
    description = getRandomString(),
    label = getRandomString(),
    originalValue = getRandomString(),
    currentValue = getRandomString()
)
