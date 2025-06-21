package com.grappim.taigamobile.core.storage.utils

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LongPreferences(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Long
) : ReadWriteProperty<Any, Long> {

    override fun getValue(thisRef: Any, property: KProperty<*>): Long = sharedPreferences
        .getLong(key, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) = sharedPreferences
        .edit {
            putLong(key, value)
        }
}

fun SharedPreferences.long(key: String, defaultValue: Long): ReadWriteProperty<Any, Long> =
    LongPreferences(this, key, defaultValue)
