package com.grappim.taigamobile.feature.workitem.mapper

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject

class JsonObjectMapper @Inject constructor() {

    fun fromMapToJsonObject(map: Map<String, Any?>): JsonObject = map.toJsonObject()

    fun Any?.toJsonElement(): JsonElement = when (this) {
        null -> JsonNull

        is JsonElement -> this

        is Boolean -> JsonPrimitive(this)

        is Number -> JsonPrimitive(this)

        is String -> JsonPrimitive(this)

        is List<*> -> JsonArray(map { it.toJsonElement() })

        is Map<*, *> -> buildJsonObject {
            forEach { (key, value) ->
                put(key.toString(), value.toJsonElement())
            }
        }

        else -> JsonPrimitive(toString())
    }

    fun Map<String, Any?>.toJsonObject(): JsonObject = buildJsonObject {
        forEach { (key, value) ->
            put(key, value.toJsonElement())
        }
    }
}
