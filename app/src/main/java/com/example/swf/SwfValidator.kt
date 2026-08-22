package com.example.swf

import com.example.swf.model.SwfFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class SwfValidator {
    
    fun validate(original: SwfFile, modified: SwfFile): Boolean {
        // A simple validation is to serialize and deserialize the modified file
        // and ensure it does not throw an exception.
        try {
            val writer = SwfWriter()
            val outStream = ByteArrayOutputStream()
            writer.write(modified, outStream)
            
            val reader = SwfReader()
            val inStream = ByteArrayInputStream(outStream.toByteArray())
            val roundTrip = reader.read(inStream)
            
            // Check essential properties
            if (roundTrip.header.signature != modified.header.signature) return false
            if (roundTrip.header.version != modified.header.version) return false
            if (roundTrip.tags.size != modified.tags.size) return false
            
            // Verify end tag exists
            if (roundTrip.tags.last().code != 0) return false
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
