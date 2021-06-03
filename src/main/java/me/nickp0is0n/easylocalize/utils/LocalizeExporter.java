package me.nickp0is0n.easylocalize.utils;

import me.nickp0is0n.easylocalize.models.LocalizedString;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class LocalizeExporter {
    public void toFile(List<LocalizedString> localizedStrings, File outputFile) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(outputFile);
        localizedStrings.forEach((localizedString -> out.println(localizedString.toString() + "\n")));
        out.close();
    }
}
