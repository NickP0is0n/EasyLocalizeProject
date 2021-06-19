package me.nickp0is0n.easylocalize.models;

import java.util.Arrays;
import java.util.List;

public class LocalizedString {
    private final String id;
    private final String text;
    private final String comment;
    private final String mark;
    private final boolean isCommentMultilined;

    public LocalizedString(String id, String text, String comment) {
        this.id = id;
        this.text = text;
        this.comment = comment;
        this.mark = null;
        this.isCommentMultilined = false;
    }

    public LocalizedString(String id, String text, String comment, boolean isCommentMultilined) {
        this.id = id;
        this.text = text;
        this.comment = comment;
        this.mark = null;
        this.isCommentMultilined = isCommentMultilined;
    }

    public LocalizedString(String id, String text, String comment, String mark, boolean isCommentMultilined) {
        this.id = id;
        this.text = text;
        this.comment = comment;
        this.mark = mark;
        this.isCommentMultilined = isCommentMultilined;
    }

    @Override
    public String toString() {
        StringBuilder rawLocalizedStringBuilder = new StringBuilder();
        if (!comment.isEmpty()) {
            if (isCommentMultilined) {
                rawLocalizedStringBuilder
                        .append("/*")
                        .append(comment.trim())
                        .append("*/")
                        .append("\n");
            }
            else {
                List<String> commentStrings = Arrays.asList(comment.split("\n"));
                commentStrings.forEach(it -> {
                    rawLocalizedStringBuilder
                            .append("//")
                            .append(it)
                            .append("\n");
                });
            }
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

    public String getMark() {
        return mark;
    }
}
