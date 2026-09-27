package ro.pontes.englishromaniandictionary;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

final class VocabularyFiles {
    static List<ErdFormat.Entry> readSection(SQLiteDatabase db, long sectionId) {
        List<ErdFormat.Entry> entries = new ArrayList<>();
        try (Cursor rows = db.rawQuery("SELECT termen,explicatie FROM vocabular "
                + "WHERE idSectiune=? ORDER BY termen COLLATE NOCASE,id",
                new String[]{Long.toString(sectionId)})) {
            while (rows.moveToNext()) entries.add(new ErdFormat.Entry(rows.getString(0), rows.getString(1)));
        }
        return entries;
    }

    static int merge(SQLiteDatabase db, String sectionName, List<ErdFormat.Entry> entries) {
        int added = 0;
        long sectionId = -1;
        long now = System.currentTimeMillis() / 1000;
        db.beginTransaction();
        try {
            try (Cursor row = db.rawQuery("SELECT id FROM sectiuni WHERE nume=?",
                    new String[]{sectionName})) {
                if (row.moveToFirst()) sectionId = row.getLong(0);
            }
            for (ErdFormat.Entry entry : entries) {
                // Preserve the application's existing global duplicate rule.
                try (Cursor row = db.rawQuery("SELECT 1 FROM vocabular WHERE termen=? AND explicatie=? LIMIT 1",
                        new String[]{entry.word, entry.explanation})) {
                    if (row.moveToFirst()) continue;
                }
                if (sectionId == -1) {
                    ContentValues section = new ContentValues();
                    section.put("nume", sectionName);
                    section.put("data", now);
                    sectionId = db.insertOrThrow("sectiuni", null, section);
                }
                ContentValues word = new ContentValues();
                word.put("idSectiune", sectionId);
                word.put("termen", entry.word);
                word.put("explicatie", entry.explanation);
                word.put("data", now);
                db.insertOrThrow("vocabular", null, word);
                added++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return added;
    }
}
