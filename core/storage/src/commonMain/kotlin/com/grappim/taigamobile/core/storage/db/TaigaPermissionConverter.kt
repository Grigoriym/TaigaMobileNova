package com.grappim.taigamobile.core.storage.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.grappim.taigamobile.core.storage.di.DbJsonQualifier
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@[ProvidedTypeConverter Single]
class TaigaPermissionConverter(@param:DbJsonQualifier private val json: Json) {

    @TypeConverter
    fun fromPermissionList(list: List<TaigaPermission>): String = json.encodeToString(list.map { it.name })

    @TypeConverter
    fun toPermissionList(value: String): List<TaigaPermission> =
        json.decodeFromString<List<String>>(value).map { TaigaPermission.valueOf(it) }
}
