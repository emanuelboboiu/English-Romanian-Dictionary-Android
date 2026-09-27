package ro.pontes.englishromaniandictionary;

/*
 * This is a database for my vocabulary.
 * Started by Manu on 25 October 2015, 22:45.
 * */

import java.io.IOException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper2 extends SQLiteOpenHelper {
    private static final String DB_NAME = "vocabulary.db"; // Database name
    private static final int DATABASE_VERSION = 2;

    public DataBaseHelper2(Context context) {
        this(context, DB_NAME);
    } // end constructor.

    // Separate database names allow device tests without touching user data.
    DataBaseHelper2(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
    }

    public void createDataBase() throws IOException {
        getWritableDatabase();
    } // end createDatabase method.

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Legacy bundled databases used user_version=0. Preserve their tables
        // and rows too; never use a preference to decide whether to replace data.
        db.execSQL("CREATE TABLE IF NOT EXISTS sectiuni ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nume varchar(128) COLLATE NOCASE, descriere varchar(1024), data INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS vocabular ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, idSectiune INTEGER NOT NULL, "
                + "termen varchar(256), explicatie varchar(1024), data INTEGER, tip INTEGER(1), "
                + "FOREIGN KEY(idSectiune) REFERENCES sectiuni(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 2 uses the existing schema; no replacement is necessary.
        // Future schema changes must use non-destructive migrations here.
    }

    /** Called only after the user confirms the vocabulary reset dialog. */
    public void resetVocabulary() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("vocabular", null, null);
            db.delete("sectiuni", null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

} // end DataBaseHelper2, a class for my vocabulary.
