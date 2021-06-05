package me.nickp0is0n.easylocalize.models;

public class LocalizedString {
    private final String id;
    private final String text;
    private final String comment;
    private final boolean isCommentMultilined;

    public LocalizedString(String id, String text, String comment) {
        this.id = id;
        this.text = text;
        this.comment = comment;
        this.isCommentMultilined = false;
    }

    public LocalizedString(String id, String text, String comment, boolean isCommentMultilined) {
        this.id = id;
        this.text = text;
        this.comment = comment;
        this.isCommentMultilined = isCommentMultilined;
    }

    @Override
    public String toString() {
        StringBuilder rawLocalizedStringBuilder = new StringBuilder();
        if (!comment.isEmpty()) {
            rawLocalizedStringBuilder
                    .append("/* ")
                    .append(comment.trim())
                    .append(" */")
                    .append("\n");
        }
        rawLocalizedStringBuilder
                .append("\"")
                .append(id)
                .append("\" = \"")
                .append(text)
                .append("\";");
        return rawLocalizedStringBuilder.toString();
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getComment() {
        return comment;
    }
}
