package com.example.swf.tags

import com.example.swf.io.BitReader
import java.io.ByteArrayOutputStream

data class DoABC(
    val flags: Long,
    val name: String,
    val abcData: ByteArray
) {
    companion object {
        fun parse(data: ByteArray): DoABC {
            val reader = BitReader(data)
            val flags = reader.readUI32()
            
            val out = ByteArrayOutputStream()
            var b = reader.readUI8()
            while (b != 0) {
                out.write(b)
                b = reader.readUI8()
            }
            val name = String(out.toByteArray(), Charsets.UTF_8)
            
            val abcData = reader.readBytes(reader.available())
            return DoABC(flags, name, abcData)
        }
    }
}
