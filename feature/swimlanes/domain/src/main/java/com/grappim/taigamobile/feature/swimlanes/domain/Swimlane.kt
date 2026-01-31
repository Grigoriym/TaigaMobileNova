package com.grappim.taigamobile.feature.swimlanes.domain

data class Swimlane(val id: Long, val name: String, val order: Long) {
    val isUnclassified: Boolean
        get() = id == UNCLASSIFIED_ID

    companion object {
        const val UNCLASSIFIED_ID = -1L

        fun unclassified() = Swimlane(
            id = UNCLASSIFIED_ID,
            name = "",
            order = 0L
        )
    }
}
