package com.example.swf

import com.example.swf.io.BitWriter
import com.example.swf.model.SwfFile
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.zip.DeflaterOutputStream

class SwfWriter {

    fun write(swfFile: SwfFile, outputStream: OutputStream) {
        val payloadWriter = BitWriter()
        swfFile.header.frameSize.write(payloadWriter)
        payloadWriter.align()
        
        val frameRateFraction = ((swfFile.header.frameRate % 1) * 256).toInt()
        val frameRateInteger = swfFile.header.frameRate.toInt()
        
        payloadWriter.writeUI8(frameRateFraction)
        payloadWriter.writeUI8(frameRateInteger)
        payloadWriter.writeUI16(swfFile.header.frameCount)
        
        for (tag in swfFile.tags) {
            val tagCode = tag.code
            val tagLength = tag.rawData.size
            
            if (tagLength >= 0x3F) {
                val tagCodeAndLength = (tagCode shl 6) or 0x3F
                payloadWriter.writeUI16(tagCodeAndLength)
                payloadWriter.writeUI32(tagLength.toLong())
            } else {
                val tagCodeAndLength = (tagCode shl 6) or tagLength
                payloadWriter.writeUI16(tagCodeAndLength)
            }
            payloadWriter.writeBytes(tag.rawData)
        }
        
        val payloadBytes = payloadWriter.toByteArray()
        val totalFileLength = 8 + payloadBytes.size.toLong()
        
        // Always write header as uncompressed first
        val magic = swfFile.header.signature.toByteArray()
        outputStream.write(magic)
        outputStream.write(swfFile.header.version)
        
        outputStream.write((totalFileLength and 0xFF).toInt())
        outputStream.write(((totalFileLength ushr 8) and 0xFF).toInt())
        outputStream.write(((totalFileLength ushr 16) and 0xFF).toInt())
        outputStream.write(((totalFileLength ushr 24) and 0xFF).toInt())
        
        if (swfFile.header.signature == "CWS") {
            val deflaterStream = DeflaterOutputStream(outputStream)
            deflaterStream.write(payloadBytes)
            deflaterStream.finish()
        } else if (swfFile.header.signature == "FWS") {
            outputStream.write(payloadBytes)
        } else {
            throw Exception("Unsupported signature for writing: ${swfFile.header.signature}")
        }
    }
}
