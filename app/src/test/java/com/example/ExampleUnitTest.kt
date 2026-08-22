package com.example

import com.example.swf.SwfReader
import com.example.swf.SwfWriter
import com.example.swf.SwfValidator
import com.example.swf.model.Rect
import com.example.swf.model.SwfFile
import com.example.swf.model.SwfHeader
import com.example.swf.model.SwfTag
import com.example.swf.tags.DefineEditText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class SwfEngineTest {
    
    @Test
    fun testDefineEditText() {
        val editText = DefineEditText(
            characterId = 1,
            bounds = Rect(0, 1000, 0, 1000),
            hasText = true,
            wordWrap = false,
            multiline = false,
            password = false,
            readOnly = false,
            hasTextColor = true,
            hasMaxLength = false,
            hasFont = true,
            hasFontClass = false,
            autoSize = false,
            hasLayout = false,
            noSelect = false,
            border = true,
            wasStatic = false,
            html = false,
            useOutlines = false,
            fontId = 2,
            fontClass = null,
            fontHeight = 200,
            textColor = 0xFF0000FF, // Blue
            maxLength = null,
            align = null,
            leftMargin = null,
            rightMargin = null,
            indent = null,
            leading = null,
            variableName = "myVar",
            initialText = "Hello SWF!"
        )
        
        val bytes = editText.toByteArray()
        val parsed = DefineEditText.parse(bytes)
        
        assertEquals(editText.characterId, parsed.characterId)
        assertEquals(editText.hasText, parsed.hasText)
        assertEquals(editText.initialText, parsed.initialText)
        assertEquals(editText.textColor, parsed.textColor)
        assertEquals(editText.variableName, parsed.variableName)
    }

    @Test
    fun testSwfHeaderAndTags() {
        val header = SwfHeader("FWS", 8, 0, Rect(0, 8000, 0, 6000), 24.0f, 1)
        val editText = DefineEditText(
            characterId = 1,
            bounds = Rect(0, 1000, 0, 1000),
            hasText = true, wordWrap = false, multiline = false, password = false, readOnly = false,
            hasTextColor = false, hasMaxLength = false, hasFont = false, hasFontClass = false, autoSize = false,
            hasLayout = false, noSelect = false, border = false, wasStatic = false, html = false, useOutlines = false,
            fontId = null, fontClass = null, fontHeight = null, textColor = null, maxLength = null, align = null,
            leftMargin = null, rightMargin = null, indent = null, leading = null,
            variableName = "test", initialText = "Hello"
        )
        val tagBytes = editText.toByteArray()
        val tag = SwfTag(37, tagBytes.size, 0, tagBytes)
        val endTag = SwfTag(0, 0, 0, ByteArray(0))
        
        val swf = SwfFile(header, mutableListOf(tag, endTag))
        
        val out = ByteArrayOutputStream()
        val writer = SwfWriter()
        writer.write(swf, out)
        
        val inStream = ByteArrayInputStream(out.toByteArray())
        val reader = SwfReader()
        val parsedSwf = reader.read(inStream)
        
        assertEquals(parsedSwf.header.signature, "FWS")
        assertEquals(parsedSwf.tags.size, 2)
        assertEquals(parsedSwf.tags[0].code, 37)
        
        val parsedEditText = DefineEditText.parse(parsedSwf.tags[0].rawData)
        assertEquals(parsedEditText.initialText, "Hello")
        
        val validator = SwfValidator()
        assertTrue(validator.validate(swf, parsedSwf))
    }
}
