package com.example.swf

import com.example.swf.io.BitReader
import com.example.swf.model.Rect
import com.example.swf.model.SwfFile
import com.example.swf.model.SwfHeader
import com.example.swf.model.SwfTag
import java.io.InputStream
import java.util.zip.InflaterInputStream
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream

class SwfReader {
    
    fun read(inputStream: InputStream): SwfFile {
        val magic = ByteArray(3)
        if (inputStream.read(magic) != 3) throw Exception("Invalid SWF: Cannot read signature")
        val signature = String(magic)
        
        val version = inputStream.read()
        
        val fileLengthBytes = ByteArray(4)
        if (inputStream.read(fileLengthBytes) != 4) throw Exception("Invalid SWF: Cannot read file length")
        
        val fileLength = (fileLengthBytes[0].toLong() and 0xFF) or
                ((fileLengthBytes[1].toLong() and 0xFF) shl 8) or
                ((fileLengthBytes[2].toLong() and 0xFF) shl 16) or
                ((fileLengthBytes[3].toLong() and 0xFF) shl 24)
        
        var payloadStream: InputStream = inputStream
        if (signature == "CWS") {
            payloadStream = InflaterInputStream(inputStream)
        } else if (signature == "ZWS") {
            throw Exception("ZWS is not currently supported")
        } else if (signature != "FWS") {
            throw Exception("Unknown SWF signature: $signature")
        }

        // Read all remaining decompressed payload into memory for parsing.
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        var read: Int
        while (payloadStream.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
        val payloadBytes = out.toByteArray()
        
        val bitReader = BitReader(payloadBytes)
        val frameSize = Rect.read(bitReader)
        bitReader.align()
        
        val frameRateFraction = bitReader.readUI8()
        val frameRateInteger = bitReader.readUI8()
        val frameRate = frameRateInteger + frameRateFraction / 256f
        
        val frameCount = bitReader.readUI16()
        
        val header = SwfHeader(signature, version, fileLength, frameSize, frameRate, frameCount)
        val tags = mutableListOf<SwfTag>()
        
        while (bitReader.available() > 0) {
            val offset = bitReader.position().toLong()
            val tagCodeAndLength = bitReader.readUI16()
            val tagCode = tagCodeAndLength ushr 6
            var tagLength = tagCodeAndLength and 0x3F
            
            if (tagLength == 0x3F) {
                tagLength = bitReader.readUI32().toInt()
            }
            
            val tagData = bitReader.readBytes(tagLength)
            tags.add(SwfTag(tagCode, tagLength, offset, tagData))
            
            if (tagCode == 0) break // End tag
        }
        
        return SwfFile(header, tags)
    }
}
