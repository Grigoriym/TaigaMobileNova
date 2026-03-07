package com.grappim.taigamobile.feature.projects.domain

data class ProjectValueItem(
    val id: Long,
    val name: String,
    val color: String = "",
    val order: Int,
    val project: Long,
    val isClosed: Boolean = false,
    val isArchived: Boolean = false,
    val value: Double? = null,
    val daysToDue: Int? = null,
    val byDefault: Boolean = false
)
