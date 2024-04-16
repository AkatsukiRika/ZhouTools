package model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object DoubleToLongSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("DoubleToLong", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Long {
        return decoder.decodeDouble().toLong()
    }

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeDouble(value.toDouble())
    }
}