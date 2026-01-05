package com.grappim.taigamobile.core.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        val instant = value.atZone(ZoneId.systemDefault()).toInstant().toString()
        encoder.encodeString(instant)
    }

    override fun deserialize(decoder: Decoder): LocalDateTime = Instant.parse(decoder.decodeString())
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}
