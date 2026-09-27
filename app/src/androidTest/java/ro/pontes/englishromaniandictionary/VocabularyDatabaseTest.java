package ro.pontes.englishromaniandictionary;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Uses disposable databases and preferences, never the user's files. */
public class VocabularyDatabaseTest {
    private String databaseName;
    private Context isolatedContext;
    private DataBaseHelper2 helper;

    private Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Before
    public void setUp() {
        databaseName = "vocabulary-test-" + System.nanoTime() + ".db";
        isolatedContext = new ContextWrapper(getContext()) {
            @Override
            public SharedPreferences getSharedPreferences(String name, int mode) {
                return super.getSharedPreferences(databaseName, mode);
            }
        };
    }

    @After
    public void tearDown() {
        if (helper != null) helper.close();
        getContext().deleteDatabase(databaseName);
        isolatedContext.getSharedPreferences("derSettings", 0).edit().clear().commit();
    }

    private SQLiteDatabase open() {
        helper = new DataBaseHelper2(isolatedContext, databaseName);
        return helper.getWritableDatabase();
    }

    private void seed(SQLiteDatabase db) {
        db.execSQL("INSERT INTO sectiuni(id,nume,data) VALUES(42,'Personal',123)");
        db.execSQL("INSERT INTO vocabular(id,idSectiune,termen,explicatie,data,tip) "
                + "VALUES(73,42,'cat','pisică',456,1)");
    }

    private void assertPreserved(SQLiteDatabase db) {
        try (Cursor row = db.rawQuery("SELECT v.id,s.nume,v.termen,v.explicatie,v.data,v.tip "
                + "FROM vocabular v JOIN sectiuni s ON s.id=v.idSectiune", null)) {
            assertTrue(row.moveToFirst());
            assertEquals(73, row.getInt(0));
            assertEquals("Personal", row.getString(1));
            assertEquals("cat", row.getString(2));
            assertEquals("pisică", row.getString(3));
            assertEquals(456, row.getInt(4));
            assertEquals(1, row.getInt(5));
            assertFalse(row.moveToNext());
        }
    }

    @Test
    public void testFreshDatabaseAndReopenWithoutPreference() {
        seed(open());
        helper.close();
        assertPreserved(open());
    }

    @Test
    public void testMismatchedPreferencesNeverReplaceVocabulary() {
        seed(open());
        helper.close();
        for (int version : new int[]{0, 1, 99}) {
            new Settings(isolatedContext).saveIntSettings("db2Ver", version);
            assertPreserved(open());
            helper.close();
        }
    }

    @Test
    public void testLegacyVersionZeroPreservesRows() {
        SQLiteDatabase db = open();
        seed(db);
        db.setVersion(0);
        helper.close();
        assertPreserved(open());
    }

    @Test
    public void testVersionOneUpgradePreservesRows() {
        SQLiteDatabase db = open();
        seed(db);
        db.setVersion(1);
        helper.close();
        assertPreserved(open());
    }

    @Test
    public void testBundledLegacyDatabasePreservesRows() throws Exception {
        try (InputStream in = getContext().getAssets().open("vocabulary.db");
             OutputStream out = new FileOutputStream(getContext().getDatabasePath(databaseName))) {
            byte[] buffer = new byte[4096];
            int size;
            while ((size = in.read(buffer)) != -1) out.write(buffer, 0, size);
        }
        try (SQLiteDatabase legacy = SQLiteDatabase.openDatabase(
                getContext().getDatabasePath(databaseName).getPath(), null,
                SQLiteDatabase.OPEN_READWRITE)) {
            seed(legacy);
        }
        assertPreserved(open());
    }

    @Test
    public void testFailedResetRollsBackAllRows() {
        SQLiteDatabase db = open();
        seed(db);
        db.execSQL("CREATE TRIGGER prevent_test_reset BEFORE DELETE ON sectiuni "
                + "BEGIN SELECT RAISE(ABORT, 'simulated failure'); END");
        try {
            helper.resetVocabulary();
            fail("Expected a reset failure");
        } catch (SQLiteException expected) {
            assertPreserved(db);
        }
    }

    @Test
    public void testExplicitResetClearsOnlyWhenCalled() {
        SQLiteDatabase db = open();
        seed(db);
        assertPreserved(db);
        helper.resetVocabulary();
        for (String table : new String[]{"vocabular", "sectiuni"}) {
            try (Cursor count = db.rawQuery("SELECT COUNT(*) FROM " + table, null)) {
                assertTrue(count.moveToFirst());
                assertEquals(0, count.getInt(0));
            }
        }
        seed(db);
        assertPreserved(db);
    }

    @Test
    public void testErdExportImportAndDuplicates() throws Exception {
        SQLiteDatabase db = open();
        seed(db);
        String exported = ErdFormat.write(VocabularyFiles.readSection(db, 42));
        java.util.List<ErdFormat.Entry> rows = ErdFormat.read(new java.io.StringReader(exported));
        assertEquals(0, VocabularyFiles.merge(db, "Duplicate section", rows));
        try (Cursor count = db.rawQuery("SELECT COUNT(*) FROM sectiuni", null)) {
            assertTrue(count.moveToFirst());
            assertEquals(1, count.getInt(0));
        }
        helper.resetVocabulary();
        assertEquals(1, VocabularyFiles.merge(db, "Imported", rows));
        assertEquals(0, VocabularyFiles.merge(db, "Imported", rows));
        try (Cursor row = db.rawQuery("SELECT termen,explicatie FROM vocabular", null)) {
            assertTrue(row.moveToFirst());
            assertEquals("cat", row.getString(0));
            assertEquals("pisică", row.getString(1));
            assertFalse(row.moveToNext());
        }
    }

    @Test
    public void testErdMergeRollsBackOnFailure() throws Exception {
        SQLiteDatabase db = open();
        seed(db);
        db.execSQL("CREATE TRIGGER prevent_import BEFORE INSERT ON vocabular "
                + "WHEN NEW.termen='fail' BEGIN SELECT RAISE(ABORT, 'simulated failure'); END");
        try {
            VocabularyFiles.merge(db, "New section", ErdFormat.read(new java.io.StringReader(
                    "dog-=-câine\nfail-=-eroare\n")));
            fail("Expected import failure");
        } catch (SQLiteException expected) {
            assertPreserved(db);
            try (Cursor count = db.rawQuery("SELECT COUNT(*) FROM sectiuni", null)) {
                assertTrue(count.moveToFirst());
                assertEquals(1, count.getInt(0));
            }
        }
    }
}
