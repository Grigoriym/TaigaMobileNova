package com.grappim.taigamobile.feature.filters.domain.model

interface Statuses {
    val color: String?
    val id: Long
    val name: String
}

data class Severity(override val color: String?, override val id: Long, override val name: String) :
    Statuses

data class Priority(override val color: String?, override val id: Long, override val name: String) :
    Statuses

data class Status(override val color: String?, override val id: Long, override val name: String) :
    Statuses

data class Type(override val color: String?, override val id: Long, override val name: String) :
    Statuses
