package com.grappim.taigamobile.feature.workitem.domain.customfield

import kotlinx.collections.immutable.ImmutableList

data class CustomFields(val fields: ImmutableList<CustomField>, val version: Long)
