package me.nickp0is0n.easylocalize.models

data class ParserModel(
    var currentComment: String = "",
    var currentId: String? = null,
    var currentString: String? = null,
    var multilineCommentMode:Boolean = false,
    var currentMark: String? = null,
    var header: String? = null,
    var settings: ParserSettings? = null
)