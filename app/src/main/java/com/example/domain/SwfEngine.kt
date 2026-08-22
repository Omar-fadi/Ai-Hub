package com.example.domain

import com.example.swf.SwfReader
import com.example.swf.SwfWriter
import com.example.swf.SwfValidator
import com.example.swf.model.SwfFile
import com.example.swf.tags.DefineEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SwfEngine {
    
    suspend fun parseSwf(file: File): SwfFile = withContext(Dispatchers.IO) {
        val reader = SwfReader()
        FileInputStream(file).use {
            reader.read(it)
        }
    }
    
    suspend fun buildSwf(original: SwfFile, output: File): Boolean = withContext(Dispatchers.IO) {
        val writer = SwfWriter()
        
        // Write to a temporary file first
        val tempFile = File(output.parent, output.name + ".tmp")
        try {
            FileOutputStream(tempFile).use {
                writer.write(original, it)
            }
            
            // Validate
            val validator = SwfValidator()
            val tempSwf = parseSwf(tempFile)
            if (validator.validate(original, tempSwf)) {
                // If valid, rename temp to output
                if (output.exists()) output.delete()
                tempFile.renameTo(output)
                return@withContext true
            } else {
                tempFile.delete()
                return@withContext false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (tempFile.exists()) tempFile.delete()
            return@withContext false
        }
    }

    fun extractEditTexts(swfFile: SwfFile): List<Pair<Int, DefineEditText>> {
        val result = mutableListOf<Pair<Int, DefineEditText>>()
        for ((index, tag) in swfFile.tags.withIndex()) {
            if (tag.code == 37) {
                try {
                    val editText = DefineEditText.parse(tag.rawData)
                    result.add(Pair(index, editText))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    fun getUnsupportedTextTags(swfFile: SwfFile): List<Int> {
        val result = mutableListOf<Int>()
        for ((index, tag) in swfFile.tags.withIndex()) {
            if (tag.code == 11 || tag.code == 33) {
                result.add(index)
            }
        }
        return result
    }

    fun applyTextModification(swfFile: SwfFile, tagIndex: Int, newText: String) {
        val tag = swfFile.tags[tagIndex]
        if (tag.code == 37) {
            val editText = DefineEditText.parse(tag.rawData)
            editText.initialText = newText
            tag.rawData = editText.toByteArray()
            tag.length = tag.rawData.size
        }
    }
}
