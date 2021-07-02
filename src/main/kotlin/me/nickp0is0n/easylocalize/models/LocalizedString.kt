package me.nickp0is0n.easylocalize.models


data class LocalizedString @JvmOverloads constructor(
    val id: String,
    val text: String,
    val comment: String,
    val isCommentMultilined: Boolean = false,
    val mark: String? = null,
    val copyrightHeader: String? = null
) {
    override fun toString(): String {
        val rawLocalizedStringBuilder = StringBuilder()
        if (comment.isNotEmpty()) {
            if (isCommentMultilined) {
                rawLocalizedStringBuilder
                    .append("/*")
                    .append(comment.trim { it <= ' ' })
                    .append("*/")
                    .append("\n")
            } else {
                val commentStrings = listOf(*comment.split("\n").toTypedArray())
                commentStrings.forEach {
                    rawLocalizedStringBuilder
                        .append("//")
                        .append(it)
                        .append("\n")
                }
            }
        }
        rawLocalizedStringBuilder
            .append("\"")
            .append(id)
            .append("\" = \"")
            .append(text)
            .append("\";")
        return rawLocalizedStringBuilder.toString()
    }

    fun toStringWithoutComment(): String {
        val rawLocalizedStringBuilder = StringBuilder()
        rawLocalizedStringBuilder
            .append("\"")
            .append(id)
            .append("\" = \"")
            .append(text)
            .append("\";")
        return rawLocalizedStringBuilder.toString()
    }
}
