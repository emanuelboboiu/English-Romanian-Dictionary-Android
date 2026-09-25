package ro.pontes.englishromaniandictionary;

import android.content.Context;
import android.database.Cursor;

public class SearchHistory {

    private final Context context;
    private final DBAdapter2 mDbHelper;

    public SearchHistory(Context context) {
        this.context = context;

        // Start things for our database, the sections one for vocabulary:
        mDbHelper = new DBAdapter2(this.context);
        mDbHelper.createDatabase();
        mDbHelper.open();

        // Create the table history if not exists:
        // type is 1 if word exists, 0 in not.
        String sql = "CREATE TABLE IF NOT EXISTS istoric(id INTEGER PRIMARY KEY AUTOINCREMENT, tip INTEGER, status INTEGER, directie INTEGER, termen VARCHAR(128) COLLATE NOCASE, data INTEGER);";
        mDbHelper.executeSQLCode(sql);
    } // end constructor.

    // A method which inserts into database a search:
    public void addRecord(String word, int direction, int type) {
        // We need the time in seconds:
        long timeInSeconds = GUITools.getTimeInSeconds();
        int status = 0; // 1 means processed item.
        String sql = "INSERT INTO istoric (tip, status, directie, termen, data) VALUES (?, ?, ?, ?, ?)";
        mDbHelper.executeSQLCode(sql, new Object[]{type, status, direction, word, timeInSeconds});
    } // end addRecord() method.

    public int getNumberOfRecords() {
        String sql = "SELECT COUNT(*) AS total FROM istoric;";
        Cursor cursor = mDbHelper.queryData(sql);
        int numberOfRecords = cursor.getInt(0);
        cursor.close();
        return numberOfRecords;
    } // end getNumberOfRecords() method.

    public void deleteSearchHistory() {
        // Make the SQL query string:
        String sql = "DELETE FROM istoric;";
        mDbHelper.deleteData(sql);
        // VACUUM also the table:
        mDbHelper.executeSQLCode("VACUUM;");
    } // end deleteSearchHistory() method.

    // A method to delete a search from history:
    public void deleteWordFromHistory(int id) {
        String sql = "DELETE FROM istoric WHERE id=?";
        mDbHelper.deleteData(sql, new Object[]{id});
    } // end deleteWordFromHistory() method.

    public Cursor getSearchesCursor(int type, int direction, String orderBy) {
        /*
         * Type 0 means not found, 1 means found words. direction 0 means
         * English, 0 means Romanian. orderBy contains a string containing the
         * name of the column to order, word or date:
         */
        String safeOrderBy = "termen ASC".equals(orderBy)
                ? "termen ASC"
                : "data DESC";
        String sql = "SELECT * FROM istoric WHERE tip=? AND directie=? ORDER BY " + safeOrderBy;
        Cursor cursor = mDbHelper.queryData(sql, new String[]{String.valueOf(type), String.valueOf(direction)});
        // Only if there are results:
        int count = cursor.getCount();
        if (count > 0) {
            return cursor;
        } // end if there were results in cursor.
        // If there are no results, getCount is 0:
        else {
            cursor.close();
            return null;
        } // end if there were no results.
    } // end getSearchesCursor() method.

} // end SearchHistory class.
