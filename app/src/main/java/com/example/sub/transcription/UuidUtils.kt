package com.example.sub.transcription

import java.nio.ByteBuffer
import java.util.*

object UuidUtils {

    /**
     * Converts the given [bytes] to a [UUID]. The given [ByteArray] should contain 16 bytes.
     */
    fun asUuid(bytes: ByteArray): UUID {
        val bb: ByteBuffer = ByteBuffer.wrap(bytes)
        val firstLong: Long = bb.long
        val secondLong: Long = bb.long
        return UUID(firstLong, secondLong)
    }


    /**
     * Converts the given [uuid] to a [ByteArray] representation. The returned [ByteArray]
     * will contain 16 bytes.
     */
    fun asBytes(uuid: UUID): ByteArray {
        val bb: ByteBuffer = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return bb.array()
    }
}