package com.xmov.ai.core.ext

fun StringBuffer.endsWithPunctuation(): Boolean {
    if (this.isEmpty()) return false

    try {
        val lastChar = this.last()
        // 中英文标点符号集合
        val punctuations = setOf(
            '.', '。', ',', '，', ';', '；', '?', '？', '!', '！'
        )
        return punctuations.contains(lastChar)
    }catch (e: Exception){
        return  false
    }

}