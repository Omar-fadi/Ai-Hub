package com.example.swf.model

data class SwfHeader(
    var signature: String, // "FWS", "CWS", "ZWS"
    var version: Int,
    var fileLength: Long,
    var frameSize: Rect,
    var frameRate: Float,
    var frameCount: Int
)

class SwfTag(
    val code: Int,
    var length: Int,
    val offset: Long,
    var rawData: ByteArray
) {
    fun isEditable(): Boolean {
        // 37 = DefineEditText, 12 = DoAction, 82 = DoABC, etc.
        return code == 37 || code == 82 || code == 39 // 39 = DefineSprite? Actually Text and ABC are what we mostly care about
    }
}

class SwfFile(
    var header: SwfHeader,
    var tags: MutableList<SwfTag>
)
