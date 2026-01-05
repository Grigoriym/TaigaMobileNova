package com.grappim.taigamobile.core.serialization

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ImmutableListSerializer<T>(dataSerializer: KSerializer<T>) : KSerializer<ImmutableList<T>> {

    private val listSerializer = ListSerializer(dataSerializer)

    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ImmutableList<T>) {
        listSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): ImmutableList<T> = listSerializer.deserialize(decoder).toImmutableList()
}
