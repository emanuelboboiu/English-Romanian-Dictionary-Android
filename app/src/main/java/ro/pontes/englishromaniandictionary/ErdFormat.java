package ro.pontes.englishromaniandictionary;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/** Legacy .erd format: UTF-8 text, one word-=-explanation pair per line. */
final class ErdFormat {
    static final int MAX_CHARS = 2_000_000;
    static final int MAX_ENTRIES = 10_000;

    static final class Entry {
        final String word;
        final String explanation;
        Entry(String word, String explanation) {
            this.word = word;
            this.explanation = explanation;
        }
    }

    static List<Entry> read(Reader reader) throws IOException {
        StringBuilder text = new StringBuilder();
        char[] buffer = new char[4096];
        int count;
        while ((count = reader.read(buffer)) != -1) {
            if (text.length() + count > MAX_CHARS) throw new IOException("File too large");
            text.append(buffer, 0, count);
        }
        if (text.length() > 0 && text.charAt(0) == '\uFEFF') text.deleteCharAt(0);
        List<Entry> entries = new ArrayList<>();
        for (String line : text.toString().split("\\r\\n|\\n|\\r")) {
            if (line.trim().isEmpty()) continue;
            String[] pair = line.split("-=-", -1);
            if (pair.length != 2) throw new IOException("Invalid entry");
            String word = pair[0].trim();
            String explanation = pair[1].trim();
            if (word.isEmpty() || explanation.isEmpty()) throw new IOException("Empty field");
            entries.add(new Entry(word, explanation));
            if (entries.size() > MAX_ENTRIES) throw new IOException("Too many entries");
        }
        if (entries.isEmpty()) throw new IOException("Empty file");
        return entries;
    }

    static String write(List<Entry> entries) throws IOException {
        if (entries.isEmpty() || entries.size() > MAX_ENTRIES) throw new IOException("Invalid size");
        StringBuilder text = new StringBuilder();
        for (Entry entry : entries) {
            // Refuse lossy export: legacy files cannot represent multiline fields or separators.
            for (String field : new String[]{entry.word, entry.explanation}) {
                if (field == null || field.trim().isEmpty() || !field.equals(field.trim())
                        || field.contains("-=-") || field.contains("\n") || field.contains("\r")
                        || field.indexOf('\uFEFF') >= 0) throw new IOException("Unrepresentable field");
            }
            text.append(entry.word).append("-=-").append(entry.explanation).append('\n');
            if (text.length() > MAX_CHARS) throw new IOException("File too large");
        }
        return text.toString();
    }
}
