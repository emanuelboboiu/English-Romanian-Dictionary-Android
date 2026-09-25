package ro.pontes.englishromaniandictionary;

/*
 * Class started on 24 September 2014 by Manu
 * Methods for statistics, like postStatistics.
 */

import android.content.Context;

public class Statistics {

    private final Context context;

    public Statistics(Context context) {
        this.context = context;
    } // end constructor.

    // A method to post a new search and the number of words searched
    // sessions:
    public void postStats(final String appIdInDB, final int numberOfSearches) {

        /*
         * Only if there is an Internet connection available, otherwise we add
         * in SharedPreferences the number of searches, this way we will add to
         * stats also the off-line searches:
         */
        if (GUITools.isNetworkAvailable(context)) {
            // Let's take the off-line searches for current ID:
            Settings set = new Settings(context);
            /*
             * We make the key in SharedPreferences as string having at the end
             * the appIdInDB:
             */
            String curKey = "offlineRecords" + appIdInDB;
            int offlineRecords = set.getIntSettings(curKey);
            /*
             * We add to off-line searches from SharedPreferences also the
             * numberOfSearches
             */
            final int totalSearches = offlineRecords + numberOfSearches;
            /*
             * Save in SharedPreferences 0 as off-line searches if was saved
             * something there for appIdInDB:
             */
            if (offlineRecords > 0) {
                set.saveIntSettings(curKey, 0);
                // We post the number of off-line statistics accumulation:
                postStats("75", offlineRecords);
            }

            String url = WebDataClient.buildUrl(
                    "https://pontes.ro/ro/divertisment/games/soft_counts.php",
                    "pid", appIdInDB,
                    "score", String.valueOf(totalSearches));

            WebDataClient.get(url, null);
        } // end if there is an Internet connection available.
        else { // No Internet available:
            /*
             * We take from SharedPreferences the offlineSearched number for
             * current ID and we add the numberOfSearchedWords, this way we have
             * a total off-line searches, we add them again in the
             * SharedPreferences:
             */
            Settings set = new Settings(context);
            String curKey = "offlineRecords" + appIdInDB;
            int offlineRecords = set.getIntSettings(curKey);
            offlineRecords = offlineRecords + numberOfSearches;
            set.saveIntSettings(curKey, offlineRecords);
        } // end if no Internet connection is available.
    }// end post data.

    public void postTestFinished(final String googleId, final String testType, final double mark) {

        String url = WebDataClient.buildUrl(
                "https://android.pontes.ro/erd/insert_test_finished.php",
                "google_id", String.valueOf(googleId),
                "tip", testType,
                "nota", String.valueOf(mark));
        WebDataClient.get(url, null);
    } // end post data for a test finished.

    // A method to change the name for mark statistics, tests finished:
    public void postNewName(final String googleId, final String newName) {
        String url = WebDataClient.buildUrl(
                "https://android.pontes.ro/erd/change_name.php",
                "google_id", String.valueOf(googleId),
                "nume", newName);

        WebDataClient.get(url, null);
    } // end postNewName() method.

} // end statistics class.
