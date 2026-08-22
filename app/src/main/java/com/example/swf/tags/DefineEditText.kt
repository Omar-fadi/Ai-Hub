package com.example.swf.tags

import com.example.swf.io.BitReader
import com.example.swf.io.BitWriter
import com.example.swf.model.Rect
import java.io.ByteArrayOutputStream

data class DefineEditText(
    val characterId: Int,
    val bounds: Rect,
    
    val hasText: Boolean,
    val wordWrap: Boolean,
    val multiline: Boolean,
    val password: Boolean,
    val readOnly: Boolean,
    val hasTextColor: Boolean,
    val hasMaxLength: Boolean,
    val hasFont: Boolean,
    val hasFontClass: Boolean,
    val autoSize: Boolean,
    val hasLayout: Boolean,
    val noSelect: Boolean,
    val border: Boolean,
    val wasStatic: Boolean,
    val html: Boolean,
    val useOutlines: Boolean,
    
    val fontId: Int?,
    val fontClass: String?,
    val fontHeight: Int?,
    val textColor: Long?,
    val maxLength: Int?,
    
    val align: Int?,
    val leftMargin: Int?,
    val rightMargin: Int?,
    val indent: Int?,
    val leading: Int?,
    
    val variableName: String,
    var initialText: String?
) {
    fun toByteArray(): ByteArray {
        val writer = BitWriter()
        writer.writeUI16(characterId)
        bounds.write(writer)
        writer.align()
        
        writer.writeBits(if (hasText) 1 else 0, 1)
        writer.writeBits(if (wordWrap) 1 else 0, 1)
        writer.writeBits(if (multiline) 1 else 0, 1)
        writer.writeBits(if (password) 1 else 0, 1)
        writer.writeBits(if (readOnly) 1 else 0, 1)
        writer.writeBits(if (hasTextColor) 1 else 0, 1)
        writer.writeBits(if (hasMaxLength) 1 else 0, 1)
        writer.writeBits(if (hasFont) 1 else 0, 1)
        
        writer.writeBits(if (hasFontClass) 1 else 0, 1)
        writer.writeBits(if (autoSize) 1 else 0, 1)
        writer.writeBits(if (hasLayout) 1 else 0, 1)
        writer.writeBits(if (noSelect) 1 else 0, 1)
        writer.writeBits(if (border) 1 else 0, 1)
        writer.writeBits(if (wasStatic) 1 else 0, 1)
        writer.writeBits(if (html) 1 else 0, 1)
        writer.writeBits(if (useOutlines) 1 else 0, 1)
        
        writer.align()
        
        if (hasFont) {
            writer.writeUI16(fontId!!)
            writer.writeUI16(fontHeight!!)
        }
        if (hasFontClass) {
            writer.writeBytes(fontClass!!.toByteArray())
            writer.writeUI8(0) // null terminator
        }
        if (hasTextColor) {
            writer.writeUI32(textColor!!)
        }
        if (hasMaxLength) {
            writer.writeUI16(maxLength!!)
        }
        if (hasLayout) {
            writer.writeUI8(align!!)
            writer.writeUI16(leftMargin!!)
            writer.writeUI16(rightMargin!!)
            writer.writeUI16(indent!!)
            writer.writeUI16(leading!!)
        }
        
        writer.writeBytes(variableName.toByteArray())
        writer.writeUI8(0)
        
        if (hasText) {
            if (initialText != null) {
                writer.writeBytes(initialText!!.toByteArray())
            }
            writer.writeUI8(0)
        }
        
        return writer.toByteArray()
    }

    companion object {
        fun parse(data: ByteArray): DefineEditText {
            val reader = BitReader(data)
            val characterId = reader.readUI16()
            val bounds = Rect.read(reader)
            reader.align()
            
            val hasText = reader.readBits(1) == 1
            val wordWrap = reader.readBits(1) == 1
            val multiline = reader.readBits(1) == 1
            val password = reader.readBits(1) == 1
            val readOnly = reader.readBits(1) == 1
            val hasTextColor = reader.readBits(1) == 1
            val hasMaxLength = reader.readBits(1) == 1
            val hasFont = reader.readBits(1) == 1
            
            val hasFontClass = reader.readBits(1) == 1
            val autoSize = reader.readBits(1) == 1
            val hasLayout = reader.readBits(1) == 1
            val noSelect = reader.readBits(1) == 1
            val border = reader.readBits(1) == 1
            val wasStatic = reader.readBits(1) == 1
            val html = reader.readBits(1) == 1
            val useOutlines = reader.readBits(1) == 1
            
            var fontId: Int? = null
            var fontHeight: Int? = null
            if (hasFont) {
                fontId = reader.readUI16()
                fontHeight = reader.readUI16()
            }
            
            var fontClass: String? = null
            if (hasFontClass) {
                fontClass = readString(reader)
            }
            
            var textColor: Long? = null
            if (hasTextColor) {
                textColor = reader.readUI32() // RGBA
            }
            
            var maxLength: Int? = null
            if (hasMaxLength) {
                maxLength = reader.readUI16()
            }
            
            var align: Int? = null
            var leftMargin: Int? = null
            var rightMargin: Int? = null
            var indent: Int? = null
            var leading: Int? = null
            if (hasLayout) {
                align = reader.readUI8()
                leftMargin = reader.readUI16()
                rightMargin = reader.readUI16()
                indent = reader.readUI16()
                leading = reader.readUI16()
            }
            
            val variableName = readString(reader)
            
            var initialText: String? = null
            if (hasText) {
                initialText = readString(reader)
            }
            
            return DefineEditText(
                characterId, bounds, hasText, wordWrap, multiline, password, readOnly, hasTextColor,
                hasMaxLength, hasFont, hasFontClass, autoSize, hasLayout, noSelect, border, wasStatic, html, useOutlines,
                fontId, fontClass, fontHeight, textColor, maxLength, align, leftMargin, rightMargin, indent, leading,
                variableName, initialText
            )
        }
        
        private fun readString(reader: BitReader): String {
            val out = ByteArrayOutputStream()
            var b = reader.readUI8()
            while (b != 0) {
                out.write(b)
                b = reader.readUI8()
            }
            return String(out.toByteArray(), Charsets.UTF_8)
        }
    }
}
