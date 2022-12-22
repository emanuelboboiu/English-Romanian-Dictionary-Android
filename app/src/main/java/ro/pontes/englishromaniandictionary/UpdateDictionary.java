package ro.pontes.englishromaniandictionary;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ro.pontes.englishromaniandictionary.R.color;

public class UpdateDictionary {

    final Context context;

    // We need a general DB object to be used in insert method:
    private final DBAdapter mDB;

    /*
     * A global static int variable for direction, it is changed in radio
     * buttons and after is used in another thread:
     */
    private int tempDirection = 0;

    // We need a constructor for context and mDB initiate:
    public UpdateDictionary(Context context) {
        this.context = context;
        // Initiate the mDB object:
        mDB = new DBAdapter(this.context);
        mDB.createDatabase();
        mDB.open();
    } // end constructor.

    public void updateStart() {
        // Only if there is an active Internet connection:
        if (GUITools.isNetworkAvailable(context)) {
            // Get the last update timestamp from SharedPreferences:
            Settings set = new Settings(context);
            int lastUpdate = set.getIntSettings("lastUpdate");
            String url = "http://www.limbalatina.ro/dictenro/new_words.php?data="
                    + lastUpdate;
            new GetUpdate().execute(url);
        } // end if there is an available Internet connection.
        else {
            GUITools.alert(
                    context,
                    context.getString(R.string.warning),
                    context.getString(R.string.no_connection_for_update_new_words));
        } // end if no Internet connection is available.
    } // end updateStart() method.

    /*
     * Here is a subclass to take and parse JSon for other resources for the
     * dictionary.
     */
    private class GetUpdate extends AsyncTask<String, String, String> {
        private ProgressDialog pd;

        // execute before task:
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setMessage(context.getString(R.string.please_wait_updating));
            pd.setIndeterminate(false);
            pd.setCancelable(true);
            pd.show();
        } // end onPreExecute() method.

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
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String line;
                // Read from the URLConnection via the BufferedReader:
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line);
                }
                bufferedReader.close();
            } catch (Exception e) {
                // e.printStackTrace();
            }
            return content.toString();
        } // end doInBackground() method.

        // Execute after task with the task result as string:
        @Override
        protected void onPostExecute(String s) {

            // Clear progress dialog:
            pd.dismiss();

            // s is the string which contains what we need:
            updateEffectively(s);
        } // end postExecute() method.
    } // end subclass GetUpdate..

    // The methods called from postExecute:
    private void updateEffectively(String s) {
        /*
         * Two strings for messages, congratulations already up to date or error
         * - or something has occurred.
         */
        String title = "";
        String message = "";

        int isErrorOrWarning = 0; // 1 means up to date, 2 error.
        if (s.length() > 0) { // no empty string is s:
            if (s.contains("updated123")) {
                // there are no updates::
                isErrorOrWarning = 1;
            } // end if updated.
            else if (s.contains("error123")) { // "error123" string in s::
                isErrorOrWarning = 2;
            } // end if not updated, but "nothing" string came.
        } // end if not an empty string there.
        else {
            isErrorOrWarning = 2; // it means error.
        } // end if it is an empty string in s.

        // We continue only if it isn't still an error or warning:
        if (isErrorOrWarning == 0) {
            String word, definition;
            int direction;

            // Do something with the interface:
            // Parse JSON data:
            // Integers for number of words inserted:
            int numberOfNewWords = 0, enro = 0, roen = 0;

            // An ArrayList for new words added to have some excerpts:
            ArrayList<String> arrWordsEn = new ArrayList<>();
            ArrayList<String> arrWordsRo = new ArrayList<>();

            try {
                JSONArray jArray = new JSONArray(s);
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jObject = jArray.getJSONObject(i);
                    direction = jObject.getInt("direction");
                    word = jObject.getString("termen");
                    definition = jObject.getString("explicatie");
                    // Insert into DB:
                    if (insertNewWordInDB(direction, word, definition)) {
                        numberOfNewWords++; // increment the total number.
                        if (direction == 0) {
                            enro++; // increment English - Romanian.
                            // Add the word into arrWordsEn ArrayList:
                            arrWordsEn.add(word);
                        } else if (direction == 1) {
                            roen++; // increment Romanian - English.
                            // Add the word into arrWordsRo ArrayList:
                            arrWordsRo.add(word);
                        }
                    } // end if successfully inserted a new word.
                } // end loop.
            } catch (JSONException e) {
                isErrorOrWarning = 2;
            } // end catch (JSONException e)
            // end parse JSON data.
            // Here announce the good update if it is not an error in catch:
            if (isErrorOrWarning == 0) {
                // Do a list of new words for English and for Romanian
                // separately:
                int limit = 20; // How many to show in new lists.

                // New English words:
                StringBuilder sbEn = new StringBuilder();
                int it = 0;
                for (int i = 0; i < arrWordsEn.size(); i++) {
                    sbEn.append(arrWordsEn.get(i));
                    sbEn.append(", ");
                    // Break if more than limit examples are in arrWords:
                    it++;
                    if (it > limit) {
                        break;
                    }
                } // end for.
                String newWordsEn = sbEn.toString();
                if (newWordsEn.length() > 0) {
                    newWordsEn = newWordsEn.substring(0,
                            newWordsEn.length() - 2);
                } else {
                    newWordsEn = ".....";
                }

                // New Romanian words:
                StringBuilder sbRo = new StringBuilder();
                it = 0;
                for (int i = 0; i < arrWordsRo.size(); i++) {
                    sbRo.append(arrWordsRo.get(i));
                    sbRo.append(", ");
                    // Break if more than limit examples are in arrWords:
                    it++;
                    if (it > limit) {
                        break;
                    }
                } // end for.
                String newWordsRo = sbRo.toString();
                if (newWordsRo.length() > 0) {
                    newWordsRo = newWordsRo.substring(0,
                            newWordsRo.length() - 2);
                } else {
                    newWordsRo = ".....";
                }

                String updMsg = String.format(
                        context.getString(R.string.updated_successfully), ""
                                + numberOfNewWords, "" + enro, "" + roen,
                        newWordsEn, newWordsRo);
                GUITools.alertHTML(context,
                        context.getString(R.string.congratulations), updMsg,
                        context.getString(R.string.msg_close));
                // Save current date in seconds as timestamp in
                // SharedPreferences:
                Calendar now = new GregorianCalendar(
                        TimeZone.getTimeZone("Europe/Bucharest"));
                int curTimestamp = (int) (now.getTimeInMillis() / 1000);
                Settings set = new Settings(context);
                set.saveIntSettings("lastUpdate", curTimestamp);

                // Add in statistics the new successful update:
                Statistics stats = new Statistics(context);
                stats.postStats("70", 1);

                // Update the number of words in DB:
                // Not yet.
            }// end if not an error in catch.
        } // end if isn't still an error.

        // No if there is an error:
        if (isErrorOrWarning > 0) {
            if (isErrorOrWarning == 1) {
                title = context.getString(R.string.information);
                // Get the last update as string:
                Settings set = new Settings(context);
                int lastUpdate = set.getIntSettings("lastUpdate");
                String tempLast = GUITools.timeStampToString(context,
                        lastUpdate);
                message = String
                        .format(context
                                        .getString(R.string.information_not_available_for_update),
                                tempLast);
                // Add in statistics the success check, no updates found:
                Statistics stats = new Statistics(context);
                stats.postStats("71", 1);

            } else if (isErrorOrWarning == 2) {
                title = context.getString(R.string.error);
                message = context.getString(R.string.error_for_update);
                // Add in statistics the not success check, an error has
                // occurred:
                Statistics stats = new Statistics(context);
                stats.postStats("72", 1);
            }

            GUITools.alertHTML(context, title, message,
                    context.getString(R.string.msg_ok));
        } // end if it is an error.
    } // end updateEffectively() method.

    // A method which inserts into database a new word:
    private boolean insertNewWordInDB(int direction, String word,
                                      String definition) {
        // We prepare the string:
        String sql = "INSERT INTO dictionar" + direction
                + " (termen, explicatie) VALUES ('" + word + "', '"
                + definition + "');";
        return mDB.insertData(sql);
    } // end insertNewWordInDB() method.

    // From here a new proposal:
    // A method to add a word:
    public void proposeStart() {
        // Make a new context:
        Context newContext = new ContextThemeWrapper(context,
                R.style.MyAlertDialog);

        if (GUITools.isNetworkAvailable(newContext)) {
            // Get the strings to make an alert:
            String tempTitle = newContext
                    .getString(R.string.title_propose_dialog);

            ScrollView sv = new ScrollView(newContext);
            LinearLayout ll = new LinearLayout(newContext);
            ll.setOrientation(LinearLayout.VERTICAL);

            // A LayoutParams to add the edit texts:
            LinearLayout.LayoutParams etlp = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            etlp.setMargins(0, MainActivity.mPaddingDP * 3, 0, 0);

            // A TextView for body of this action:
            TextView tv = new TextView(newContext);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
            String message = newContext
                    .getString(R.string.message_propose_dialog);
            tv.setText(message);
            tv.setFocusable(true);
            ll.addView(tv, etlp);

            // EditText for word:
            final EditText etWord = new EditText(newContext);
            etWord.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                    MainActivity.textSize);
            etWord.setPadding(MainActivity.mPaddingDP, MainActivity.mPaddingDP,
                    MainActivity.mPaddingDP, MainActivity.mPaddingDP);
            etWord.setHint(newContext.getString(R.string.hint_propose_new_word));
            etWord.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
                    | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            etWord.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            ll.addView(etWord, etlp);

            // EditText for explanation:
            final EditText etExplanation = new EditText(newContext);
            etExplanation.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                    MainActivity.textSize);
            etExplanation.setPadding(MainActivity.mPaddingDP,
                    MainActivity.mPaddingDP, MainActivity.mPaddingDP,
                    MainActivity.mPaddingDP);
            etExplanation.setHint(newContext
                    .getString(R.string.hint_propose_new_explanation));
            etExplanation
                    .setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
                            | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            etExplanation.setImeOptions(EditorInfo.IME_ACTION_DONE);
            etExplanation
                    .setOnEditorActionListener((v, actionId, event) -> {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            // Nothing happens yet, only hide the keyboard.
                        } // end if DONE key was pressed.
                        return false;
                    });
            // End add action listener.
            ll.addView(etExplanation, etlp);

            // Now add two radio buttons for EnRo or RoEn:
            RadioGroup radioGroup = new RadioGroup(newContext);
            RadioButton rb0 = new RadioButton(newContext);
            rb0.setHintTextColor(color.black);
            rb0.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
            rb0.setText(newContext.getString(R.string.rb_en_ro));
            rb0.setTextColor(Color.BLACK);
            rb0.setBackgroundColor(Color.WHITE);
            rb0.setFocusable(true);
            rb0.setOnClickListener(v -> tempDirection = 0);
            radioGroup.addView(rb0, etlp);

            RadioButton rb1 = new RadioButton(newContext);
            rb1.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
            rb1.setText(newContext.getString(R.string.rb_ro_en));
            rb1.setTextColor(Color.BLACK);
            rb1.setBackgroundColor(Color.WHITE);
            rb1.setFocusable(true);
            rb1.setOnClickListener(v -> tempDirection = 1);
            radioGroup.addView(rb1, etlp);
            ll.addView(radioGroup, etlp);
            sv.addView(ll);

            // end if send button was pressed.
            AlertDialog.Builder alert = new AlertDialog.Builder(context)
                    .setTitle(tempTitle)
                    .setView(sv)
                    .setPositiveButton(R.string.send,
                            (dialog, whichButton) -> {
                                String newWord = etWord.getText()
                                        .toString();
                                String newExplanation = etExplanation
                                        .getText().toString();
                                sendAdd(tempDirection, newWord,
                                        newExplanation);
                            }).setNegativeButton(android.R.string.cancel, null);

            alert.create();
            alert.show();
        } else {
            GUITools.alert(
                    newContext,
                    newContext.getString(R.string.warning),
                    newContext
                            .getString(R.string.no_connection_for_propose_new_words));
        }
    } // end add Record() method.

    // A method to send effectively the addition from method above:
    private void sendAdd(int direction, String word, String explanation) {
        boolean toReturn;
        // Save now the new record:
        // First of all, check if both edit text have text:
        String newWord = (MyHtml.fromHtml(word).toString()).trim();
        String newExplanation = (MyHtml.fromHtml(explanation).toString())
                .trim();
        if (newWord.length() >= 2 && newExplanation.length() >= 2) {
            // Add here into online database:
            String url = "http://www.limbalatina.ro/dictenro/new_words_proposals.php?direction="
                    + direction
                    + "&google_id="
                    + MainActivity.myAccountName
                    + "&termen=" + newWord + "&explicatie=" + newExplanation;
            new SendUpdate().execute(url);
            toReturn = true;
        } // end if the length are OK.
        else {
            GUITools.alert(context, context.getString(R.string.warning),
                    context.getString(R.string.no_texts_for_edit));
            toReturn = false;
        } // end if edit text haven't text.
    } // end send new add Edit() method.

    // A subclass to send data to server:
    private class SendUpdate extends AsyncTask<String, String, String> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setMessage(context.getString(R.string.please_wait_sending));
            pd.setIndeterminate(false);
            pd.setCancelable(true);
            pd.show();
        } // end onPreExecute() method.

        @Override
        protected String doInBackground(String... params) {
            StringBuilder content = new StringBuilder();
            String urlString = params[0]; // URL to call
            try {
                // Create a URL object:
                URL url = new URL(urlString);
                // Create a URLConnection object:
                URLConnection urlConnection = url.openConnection();
                // Wrap the URLConnection in a BufferedReader:
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String line;
                // Read from the URLConnection via the BufferedReader:
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line);
                }
                bufferedReader.close();
            } catch (Exception e) {
                // e.printStackTrace();
            }
            return content.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            // Clear progress dialog:
            pd.dismiss();

            if (result.contains("successfully")) {
                GUITools.alert(context, context.getString(R.string.success),
                        context.getString(R.string.word_sent_successfully));
            } else {
                GUITools.alert(context, context.getString(R.string.error),
                        context.getString(R.string.word_not_sent_successfully));
            }
        } // end postExecute() method.

    } // end subclass SendUpdate.

} // end UpdateDictionary class.
