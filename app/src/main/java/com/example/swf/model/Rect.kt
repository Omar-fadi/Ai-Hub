package com.example.swf.model

import com.example.swf.io.BitReader
import com.example.swf.io.BitWriter
import kotlin.math.max

data class Rect(
    val xMin: Int,
    val xMax: Int,
    val yMin: Int,
    val yMax: Int
) {
    fun write(writer: BitWriter) {
        val maxVal = max(
            max(Math.abs(xMin), Math.abs(xMax)),
            max(Math.abs(yMin), Math.abs(yMax))
        )
        // Find required bits (including sign bit)
        var nBits = 0
        var temp = maxVal
        while (temp > 0) {
            nBits++
            temp = temp ushr 1
        }
        nBits++ // For sign bit
        if (nBits < 5) nBits = 5 // Minimum 5 bits if required, but SWF spec says NBits is 5 bits itself.
        
        writer.writeBits(nBits, 5)
        writer.writeSignedBits(xMin, nBits)
        writer.writeSignedBits(xMax, nBits)
        writer.writeSignedBits(yMin, nBits)
        writer.writeSignedBits(yMax, nBits)
    }

    companion object {
        fun read(reader: BitReader): Rect {
            val nBits = reader.readBits(5)
            val xMin = reader.readSignedBits(nBits)
            val xMax = reader.readSignedBits(nBits)
            val yMin = reader.readSignedBits(nBits)
            val yMax = reader.readSignedBits(nBits)
            return Rect(xMin, xMax, yMin, yMax)
        }
    }
}
