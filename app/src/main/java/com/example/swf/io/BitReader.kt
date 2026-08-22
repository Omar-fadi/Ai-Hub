package com.example.swf.io

class BitReader(private val bytes: ByteArray) {
    private var bytePos = 0
    private var bitPos = 0

    fun readBits(count: Int): Int {
        var value = 0
        var bitsRemaining = count

        while (bitsRemaining > 0) {
            if (bytePos >= bytes.size) return value // Or throw Exception

            val bitsAvailable = 8 - bitPos
            val bitsToRead = if (bitsRemaining > bitsAvailable) bitsAvailable else bitsRemaining

            val mask = (1 shl bitsToRead) - 1
            val shift = 8 - bitPos - bitsToRead

            val part = (bytes[bytePos].toInt() ushr shift) and mask
            value = (value shl bitsToRead) or part

            bitPos += bitsToRead
            bitsRemaining -= bitsToRead

            if (bitPos == 8) {
                bitPos = 0
                bytePos++
            }
        }
        return value
    }

    fun readSignedBits(count: Int): Int {
        if (count == 0) return 0
        var value = readBits(count)
        val signBit = 1 shl (count - 1)
        if ((value and signBit) != 0) {
            // Sign extend
            val mask = -1 shl count
            value = value or mask
        }
        return value
    }

    fun align() {
        if (bitPos > 0) {
            bitPos = 0
            bytePos++
        }
    }

    fun readUI8(): Int {
        align()
        if (bytePos >= bytes.size) return 0
        return bytes[bytePos++].toInt() and 0xFF
    }

    fun readUI16(): Int {
        align()
        if (bytePos + 1 >= bytes.size) return 0
        val b0 = bytes[bytePos++].toInt() and 0xFF
        val b1 = bytes[bytePos++].toInt() and 0xFF
        return b0 or (b1 shl 8)
    }

    fun readUI32(): Long {
        align()
        if (bytePos + 3 >= bytes.size) return 0
        val b0 = bytes[bytePos++].toLong() and 0xFF
        val b1 = bytes[bytePos++].toLong() and 0xFF
        val b2 = bytes[bytePos++].toLong() and 0xFF
        val b3 = bytes[bytePos++].toLong() and 0xFF
        return b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
    }

    fun readBytes(length: Int): ByteArray {
        align()
        if (bytePos + length > bytes.size) {
            throw IllegalArgumentException("Unexpected end of tag data. Required: $length, Available: ${bytes.size - bytePos}")
        }
        val result = ByteArray(length)
        System.arraycopy(bytes, bytePos, result, 0, length)
        bytePos += length
        return result
    }

    fun position(): Int = bytePos
    fun length(): Int = bytes.size
    fun available(): Int = bytes.size - bytePos
}
