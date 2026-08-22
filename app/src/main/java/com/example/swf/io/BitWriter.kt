package com.example.swf.io

import java.io.ByteArrayOutputStream

class BitWriter {
    private val out = ByteArrayOutputStream()
    private var currentByte = 0
    private var bitPos = 0

    fun writeBits(value: Int, count: Int) {
        var bitsRemaining = count
        while (bitsRemaining > 0) {
            val bitsAvailable = 8 - bitPos
            val bitsToWrite = if (bitsRemaining > bitsAvailable) bitsAvailable else bitsRemaining

            val shift = bitsRemaining - bitsToWrite
            val mask = (1 shl bitsToWrite) - 1
            val part = (value ushr shift) and mask

            currentByte = currentByte or (part shl (8 - bitPos - bitsToWrite))

            bitPos += bitsToWrite
            bitsRemaining -= bitsToWrite

            if (bitPos == 8) {
                out.write(currentByte)
                currentByte = 0
                bitPos = 0
            }
        }
    }

    fun writeSignedBits(value: Int, count: Int) {
        // Just write the lower 'count' bits. The sign bit is already where it should be.
        val mask = if (count == 32) -1 else (1 shl count) - 1
        writeBits(value and mask, count)
    }

    fun align() {
        if (bitPos > 0) {
            out.write(currentByte)
            currentByte = 0
            bitPos = 0
        }
    }

    fun writeUI8(value: Int) {
        align()
        out.write(value and 0xFF)
    }

    fun writeUI16(value: Int) {
        align()
        out.write(value and 0xFF)
        out.write((value ushr 8) and 0xFF)
    }

    fun writeUI32(value: Long) {
        align()
        out.write((value and 0xFF).toInt())
        out.write(((value ushr 8) and 0xFF).toInt())
        out.write(((value ushr 16) and 0xFF).toInt())
        out.write(((value ushr 24) and 0xFF).toInt())
    }

    fun writeBytes(bytes: ByteArray) {
        align()
        out.write(bytes, 0, bytes.size)
    }

    fun toByteArray(): ByteArray {
        align()
        return out.toByteArray()
    }
}
