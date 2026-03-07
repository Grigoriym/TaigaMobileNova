package com.grappim.taigamobile.utils.ui.serializers

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ColorSerializer : KSerializer<Color> {

    override fun deserialize(decoder: Decoder): Color {
        val string = decoder.decodeString()

        val ulong = string.toULong()
        return Color(ulong)
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            serialName = "androidx.compose.ui.graphics.Color",
            kind = PrimitiveKind.LONG
        )

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeString(value.value.toString())
    }
}
