package me.nickp0is0n.easylocalize.utils;

import me.nickp0is0n.easylocalize.models.LocalizedString;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalizeParser {

    private String currentComment = "";
    private String currentId = null;
    private String currentString = null;
    private boolean multilineCommentMode = false;
    private String currentMark = null;

    private final ArrayList<LocalizedString> strings = new ArrayList<>();

    public List<LocalizedString> fromFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String currentLine;
        boolean isCommentMultilined = false;

        while ((currentLine=reader.readLine())!=null) {
            if (isAComment(currentLine)) {
                currentComment = currentComment + parseComment(currentLine);
                if (multilineCommentMode) {
                    isCommentMultilined = true;
                }
            }

            else if (isLineBelongToUnfinishedString(currentLine, currentString)) {
                parseEndOfMultilineString(currentLine);
            }

            else if (!currentLine.equals("") || multilineCommentMode) {
                Pattern wrappedStringPattern = Pattern.compile("([\"])(?:(?=(\\\\?))\\2.)*?\\1");
                Matcher patternMatcher = wrappedStringPattern.matcher(currentLine);

                retrieveId(currentLine, patternMatcher);
                retrieveTextString(currentLine, patternMatcher);

                finalizeLocalizedString(currentLine, isCommentMultilined);
                isCommentMultilined = false;
            }
        }
        return strings;
    }

    private void finalizeLocalizedString(String currentLine, boolean isCommentMultilined) {
        if (currentLine.endsWith("\";")) {
            strings.add(new LocalizedString(currentId, currentString, currentComment, currentMark, isCommentMultilined));
            currentComment = "";
        }
    }

    private boolean isLineBelongToUnfinishedString(String currentLine, String currentString) {
        return !currentLine.startsWith("\"") && currentString != null;
    }

    private boolean isAComment(String currentLine) {
        return currentLine.startsWith("//") || currentLine.startsWith("/*") || multilineCommentMode;
    }

    private void retrieveId (String currentLine, Matcher patternMatcher) throws IOException {
        if (!patternMatcher.find()) {
            throw new IOException("ID is not found");
        }
        currentId = currentLine.substring(patternMatcher.start() + 1, patternMatcher.end() - 1);
    }

    private void retrieveTextString(String currentLine, Matcher patternMatcher) {
        if (!patternMatcher.find()) {
            int lastIndex = nthIndexOf(currentLine, '\"', 3);
            currentString = currentLine.substring(lastIndex + 1);
        }
        else {
            currentString = currentLine.substring(patternMatcher.start() + 1, patternMatcher.end() - 1);
        }
    }

    private void parseEndOfMultilineString(String currentLine) {
        //String currentString = "";
        if (currentLine.endsWith("\";")) {
            currentLine = currentLine.substring(0, currentLine.length() - 2);
            currentString = currentString + "\n" + currentLine;
            strings.add(new LocalizedString(currentId, currentString, currentComment));
            currentComment = "";
        }
        else {
            currentString = currentString + "\n" + currentLine;
        }
    }

    private String parseComment(String currentLine) {
        String currentComment = "";
        if (currentLine.startsWith("//")) {
            if (currentLine.contains("MARK:")) {
                currentMark = currentLine.substring(8);
                return currentComment;
            }
            currentComment = currentComment + currentLine.substring(2) + "\n";
            return currentComment;
        }
        if (currentLine.startsWith("/*")) {
            multilineCommentMode = true;
        }
        if (multilineCommentMode) {
            currentLine = currentLine.replace("/*", "");
            currentComment = currentComment + currentLine + "\n";
            if (currentLine.endsWith("*/")) {
                currentComment = currentComment.substring(0, currentComment.length() - 3);
                multilineCommentMode = false;
            }
        }
        return currentComment;
    }

    private int nthIndexOf(String text, char needle, int n)
    {
        for (int i = 0; i < text.length(); i++)
        {
            if (text.charAt(i) == needle)
            {
                n--;
                if (n == 0)
                {
                    return i;
                }
            }
        }
        return -1;
    }
}
