package com.grappim.taigamobile.feature.workitem.domain.customfield

import kotlinx.collections.immutable.ImmutableList

data class CustomField(
    val id: Long,
    val type: CustomFieldType,
    val name: String,
    val description: String? = null,
    val value: CustomFieldValue?,
    // for CustomFieldType.Dropdown
    val options: ImmutableList<String>? = null
)
