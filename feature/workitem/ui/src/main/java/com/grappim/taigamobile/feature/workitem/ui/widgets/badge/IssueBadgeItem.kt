package com.grappim.taigamobile.feature.workitem.ui.widgets.badge

import androidx.compose.ui.graphics.Color

sealed interface IssueBadgeItem {
    val title: String
    val color: Color
    val onClick: () -> Unit
    val isLoading: Boolean
    val isClickable: Boolean
}

data class StatusBadgeItem(
    override val title: String,
    override val color: Color,
    override val onClick: () -> Unit,
    override val isLoading: Boolean,
    override val isClickable: Boolean
) : IssueBadgeItem

data class TypeBadgeItem(
    override val title: String,
    override val color: Color,
    override val onClick: () -> Unit,
    override val isLoading: Boolean,
    override val isClickable: Boolean
) : IssueBadgeItem

data class SeverityBadgeItem(
    override val title: String,
    override val color: Color,
    override val onClick: () -> Unit,
    override val isLoading: Boolean,
    override val isClickable: Boolean
) : IssueBadgeItem

data class PriorityBadgeItem(
    override val title: String,
    override val color: Color,
    override val onClick: () -> Unit,
    override val isLoading: Boolean,
    override val isClickable: Boolean
) : IssueBadgeItem
