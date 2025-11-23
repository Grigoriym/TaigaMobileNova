package com.grappim.taigamobile.core.storage

enum class ThemeSettings(val value: String) {
    System("system"),
    Light("light"),
    Dark("dark");

    companion object {
        fun fromValue(value: String?): ThemeSettings? = ThemeSettings.entries.firstOrNull { it.value == value }

        fun default() = System
    }
}

fun ThemeSettings.isSystemDefault() = this == ThemeSettings.System

fun ThemeSettings.isDark() = this == ThemeSettings.Dark

fun ThemeSettings.isLight() = this == ThemeSettings.Light
