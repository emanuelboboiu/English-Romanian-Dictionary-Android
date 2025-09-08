package ro.pontes.englishromaniandictionary;

/*
 * Class started on 24 September 2014 by Manu
 * Methods for statistics, like postStatistics.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.os.AsyncTask;

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

            String url = "https://pontes.ro/ro/divertisment/games/soft_counts.php?pid=" + appIdInDB + "&score=" + totalSearches;

            new GetWebData().execute(url);
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

        String url = "https://android.pontes.ro/erd/insert_test_finished.php?google_id=" + googleId + "&tip=" + testType + "&nota=" + mark;
        new GetWebData().execute(url);
    } // end post data for a test finished.

    // A method to change the name for mark statistics, tests finished:
    public void postNewName(final String googleId, final String newName) {
        String url = "https://android.pontes.ro/erd/change_name.php?google_id=" + googleId + "&nume=" + newName;

        new GetWebData().execute(url);
    } // end postNewName() method.

    // This is a subclass:
    private static class GetWebData extends AsyncTask<String, String, String> {

        // execute before task:
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Execute task
        String urlText = "";

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder content = new StringBuilder();
            urlText = strings[0];
            try {
                // Create a URL object:
                URL url = new URL(urlText);
                // Create a URLConnection object:
                URLConnection urlConnection = url.openConnection();
                // Wrap the URLConnection in a BufferedReader:
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                // Read from the URLConnection via the BufferedReader:
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line);
                }
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return content.toString();
        } // end doInBackground() method.

        // Execute after task with the task result as string:
        @Override
        protected void onPostExecute(String s) {
            // Do nothing yet.
        } // end postExecute() method.
    } // end subclass.

} // end statistics class.
