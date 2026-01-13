package ro.pontes.englishromaniandictionary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;


public class VerbsActivity extends Activity implements OnItemSelectedListener {

    private boolean isDerivedForms = true;
    private boolean isArchaicForms = true;
    private String type = "0"; // means non_derived forms.

    // Some static fields for general average:
    public static double average = 0.0;
    public static double sumOfMarks = 0.0;
    public static int numberOfTests = 0;
    public static int limitForLastMarks = 5;
    public static ArrayList<String> arLastXMarks;
    public static int numberOfTestsInSession = 0;

    private DBAdapter mDbHelper;
    private int numberOfVerbs = 0;
    private SpeakText speak;

    private final Context mFinalContext = this;

    // Creating object of AdView:
    private AdView bannerAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * We charge different layouts depending of the premium status or
         * android TV:
         */
        if (MainActivity.isTV) {
            if (MainActivity.isPremium) {
                setContentView(R.layout.activity_verbs_premium_tv);
            } else {
                setContentView(R.layout.activity_verbs_tv);
            }
        } else {
            // No if is not a TV:
            if (MainActivity.isPremium) {
                setContentView(R.layout.activity_verbs_premium);
            } else {
                setContentView(R.layout.activity_verbs);
            }
            // Set the orientation to be portrait if needed:
            if (MainActivity.isOrientationBlocked) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } // end if isOrientationBlocked is true.
        } // end if else, is not a TV.
        // end charging the correct layout.

        // Check or check the check boxes, depending of current boolean values:
        Settings set = new Settings(this);
        isDerivedForms = set.getBooleanSettings("isDerivedForms");
        isArchaicForms = set.getBooleanSettings("isArchaicForms");
        // For derived forms:
        CheckBox cbtDerivedForms = (CheckBox) findViewById(R.id.cbtDerivedForms);
        cbtDerivedForms.setChecked(isDerivedForms);
        // For archaic forms:
        CheckBox cbtArchaicForms = (CheckBox) findViewById(R.id.cbtArchaicForms);
        cbtArchaicForms.setChecked(isArchaicForms);
        // end check boxes.
        if (isDerivedForms) {
            type = "%";
        } else {
            type = "0";
        }

        // Start things for our database:
        mDbHelper = new DBAdapter(this);
        mDbHelper.createDatabase();
        mDbHelper.open();

        /*
         * Update the number of verbs message, under drop down. It is important
         * to call this method first because it fills also a global variable
         * with number of verbs.
         */
        updateWelcomeMessage();

        // Call the method which fills the spinner:
        updateSpinner();

        // Call the method to show banner if is not premium:
        if (!MainActivity.isPremium) {
            bannerAdView = findViewById(R.id.bannerAdView);
            adMobSequence();
        }

        // Initialise the ArrayList for last marks:
        arLastXMarks = new ArrayList<>();
        // GUITools.alert(this, "", "" + arLastXMarks.size());
        // To keep screen awake:
        if (MainActivity.isWakeLock) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } // end wake lock.

        speak = new SpeakText(this);

    } // end onCreate() method.

    protected void onResume() {
        super.onResume();
        // Some initial things like background:
        GUITools.setLayoutInitial(this, 1);
    } // end onResume() method.

    // A method to update the text view with welcome message:
    private void updateWelcomeMessage() {

        // The number of basic forms in DB:
        String sql = "SELECT count(id) FROM verbe WHERE tip=0;";
        Cursor cursor = mDbHelper.queryData(sql);
        int numberOfNonDerivedVerbs = cursor.getInt(0);

        // The number of derivative forms in DB:
        sql = "SELECT count(id) FROM verbe WHERE tip=1;";
        cursor = mDbHelper.queryData(sql);
        int numberOfDerivedVerbs = cursor.getInt(0);
        // Make the total :
        int numberOfTotalVerbs = numberOfNonDerivedVerbs + numberOfDerivedVerbs;
        if (type.equals("%")) {
            numberOfVerbs = numberOfNonDerivedVerbs + numberOfDerivedVerbs;
        } else {
            numberOfVerbs = numberOfNonDerivedVerbs;
        }
        cursor.close();

        TextView tv = (TextView) findViewById(R.id.tvNumberOfVerbs);
        String message = String.format(getString(R.string.tv_welcome_verbs_message), "" + "" + numberOfTotalVerbs, "" + numberOfNonDerivedVerbs, "" + numberOfDerivedVerbs);
        tv.setText(MyHtml.fromHtml(message));
        tv.setFocusable(true);
    } // end updateWelcomeMessage() method.

    // Update the drop down list:
    private void updateSpinner() {

        // Get the verbs starting with a letter and number of them.
        // It depends if all forms or only non-derived must be shown:
        String sql = "SELECT DISTINCT(SUBSTR(forma1, 1, 1)) AS initiale FROM verbe WHERE tip LIKE '" + type + "' ORDER BY initiale";
        Cursor cursor = mDbHelper.queryData(sql);

        // Create a delimited string:

        StringBuilder sb = new StringBuilder(getString(R.string.in_spinner_choose_a_letter));
        sb.append("|");
        sb.append(String.format(getString(R.string.in_spinner_all_initials), "" + numberOfVerbs));

        cursor.moveToFirst();
        do {
            sb.append("|");
            String firstLetter = cursor.getString(0);
            sb.append(firstLetter);
            sb.append(" - ");
            // Get number of verbs with this letter:
            Cursor cursor2 = mDbHelper.queryData("SELECT COUNT(forma1) FROM verbe WHERE tip LIKE '" + type + "' AND forma1 LIKE '" + firstLetter + "%'");
            int nr = cursor2.getInt(0);
            sb.append(nr);
            cursor2.close();
        } while (cursor.moveToNext());
        // end do ... while.
        cursor.close();

        Spinner dropdown = (Spinner) findViewById(R.id.spinnerChoose);
        String[] items = sb.toString().split("\\|");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
    } // end updateSpinner() method.

    @Override
    public void onDestroy() {
        mDbHelper.close();
        // Shut down also the TTS:
        speak.close();
        super.onDestroy();
    } // end onDestroy method.

    @Override
    public void onBackPressed() {
        this.finish();
        GUITools.goToDictionary(this);
    } // end onBackPressed()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.verbs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.mnuGoToDictionary) {
            this.finish();
            GUITools.goToDictionary(this);
        } else if (id == R.id.mnuVocabulary) {
            this.finish();
            GUITools.goToVocabulary(this);
        } else if (id == R.id.mnuViewStatistics) {
            viewStatistics();
        } // end if view statistics was pressed in menu.
        else if (id == R.id.mnuViewOnlineStatistics) {
            GUITools.viewOnlineStatistics(this);
        } // end if view statistics was pressed in menu.
        else if (id == R.id.mnuNickname) {
            beforeChangeNickname();
        } // end if change nickname was pressed in menu.
        else if (id == R.id.mnuHelp) {
            GUITools.showHelp(this);
        } // end if Help is chosen in main menu.
        else if (id == R.id.mnuAboutDialog) {
            GUITools.aboutDialog(this);
        } // end if about dictionary is chosen in main menu.
        else if (id == R.id.mnuRate) {
            GUITools.showRateDialog(this);
        } // end if rate option was chosen in menu.

        return super.onOptionsItemSelected(item);
    } // end item selection in menu.

    // A method to create an alert to view statistics:
    private void viewStatistics() {

        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);

        ScrollView sv = new ScrollView(context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        int mPaddingDP = MainActivity.mPaddingDP;

        TextView tv;

        // Number of tests tried:
        tv = new TextView(context);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
        tv.setPadding(0, mPaddingDP, 0, mPaddingDP);
        tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tv.setFocusable(true);

        // Make a correct string for number of tests:
        Resources res = getResources();
        String testsFinished = res.getQuantityString(R.plurals.tv_number_of_tests, numberOfTests, numberOfTests);
        tv.setText(testsFinished);
        tv.setFocusable(true);
        ll.addView(tv);

        // General average:
        tv = new TextView(context);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
        tv.setPadding(0, mPaddingDP, 0, mPaddingDP);
        tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tv.setFocusable(true);
        if (numberOfTests == 0) {
            tv.setText(getString(R.string.tv_no_average));
            tv.setFocusable(true);
        } else {
            tv.setText(String.format(getString(R.string.tv_general_average), "" + GUITools.round(average, 2)));
            tv.setFocusable(true);
        }
        ll.addView(tv);

        // The zone for average and lastXMarks history:
        getLastXMarks(this); // this fills an ArrayList of strings.

        /*
         * Make now an array two dimensions of string from ArrayList. One
         * dimension is for mark, one for date and time.
         */
        double averageOfLastXMarks = 0.0;
        String[][] aMarksAndTime = new String[2][2]; // just to initialise.
        /*
         * Only if arLastXMarks size is greater than 0 or at 0 is not initial
         * message, that about no history:
         */
        /*
         * We need also to know if at index 0 there is the initial message, that
         * about no last marks in history:
         */
        String tempMsg = String.format(getString(R.string.no_last_x_marks), "" + limitForLastMarks);
        boolean isNotLastMarks = arLastXMarks.get(0).equals(tempMsg);
        if (!isNotLastMarks) {
            aMarksAndTime = new String[arLastXMarks.size()][2];
            // Fill now that two dimensions array:
            // In the first dimension is the mark, in the second one is the
            // date:
            for (int i = 0; i < arLastXMarks.size(); i++) {
                String[] curEntry = arLastXMarks.get(i).split("-");
                aMarksAndTime[i][0] = curEntry[0];
                aMarksAndTime[i][1] = curEntry[1];
            } // end for fill two dimensions array.

            // Calculate the average for lastXMarks:
            double sumOfLastXMarks = 0.0;
            for (int i = 0; i < arLastXMarks.size(); i++) {
                sumOfLastXMarks += Float.parseFloat(aMarksAndTime[i][0]);
            } // end for calculate the average.

            averageOfLastXMarks = sumOfLastXMarks / (float) arLastXMarks.size();
            // Round it to two decimals:
            averageOfLastXMarks = GUITools.round(averageOfLastXMarks, 2);
        } // end if there are marks as last.

        // Show now the title of this subsection:
        tv = new TextView(context);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
        tv.setPadding(0, mPaddingDP, 0, mPaddingDP);
        tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tv.setFocusable(true);
        if (isNotLastMarks) {
            tv.setText(String.format(getString(R.string.no_average_for_last_marks), "" + limitForLastMarks));
            tv.setFocusable(true);
        } else {
            tv.setText(String.format(getString(R.string.tv_average_of_last_marks), "" + arLastXMarks.size(), "" + averageOfLastXMarks));
            tv.setFocusable(true);
        }
        ll.addView(tv);
        // End show the average for lastXMarks.

        // Show now the history of last marks:
        for (int i = 0; i < arLastXMarks.size(); i++) {
            tv = new TextView(context);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
            tv.setPadding(0, mPaddingDP, 0, mPaddingDP);
            tv.setFocusable(true);
            if (isNotLastMarks) {
                tv.setText(String.format(getString(R.string.no_last_x_marks), "" + limitForLastMarks));
            } else {
                tv.setText(String.format(getString(R.string.one_of_last_marks), aMarksAndTime[i][0], GUITools.timeStampToString(this, Integer.parseInt(aMarksAndTime[i][1]))));
            }
            // Add it into the LinearLayout:
            ll.addView(tv);
        } // end for.

        /*
         * Add an information at the alert bottom about local status, not all
         * statistics are here:
         */
        tv = new TextView(context);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
        tv.setPadding(0, mPaddingDP * 2, 0, mPaddingDP);
        tv.setFocusable(true);
        tv.setText(getString(R.string.local_stats_bottom_info));

        // Add it into the LinearLayout:
        ll.addView(tv);

        // Add the LinearLayout into ScrollView:
        sv.addView(ll);

        // Create now the alert:
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(getString(R.string.title_view_statistics));
        alertDialog.setView(sv);
        alertDialog.setPositiveButton(getString(R.string.bt_close), null);
        AlertDialog alert = alertDialog.create();
        alert.show();
    } // end viewStatistics() method.

    // The method called at list button press:
    public void restartThisActivity(View view) {
        recreateThisActivity();
    } // end restartThisActivity() method.

    // A method which recreates this activity:
    private void recreateThisActivity() {
        SoundPlayer.playSimple(this, "go_to_left");
        startActivity(getIntent());
        finish();
    } // end recreateThisActivity() method.

    public void onPause() {
        speak.stop();
        if (numberOfTestsInSession > 0) {
            Statistics stats = new Statistics(this);
            stats.postStats("28", numberOfTestsInSession);
            numberOfTestsInSession = 0;
        } // end if are tests to post.
        super.onPause();
    } // end onPause() method.

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            // Do nothing, nothing was selected.
        } else if (position == 1) {
            createList("");
        } else {
            String chosen = parent.getItemAtPosition(position).toString();
            String[] aChosen = chosen.split(" - ");
            String chosenInitial = aChosen[0];
            createList(chosenInitial);
        } // end if position is greater than 1, an initial.
    } // end onItemSelected() implemented method.

    // The method which writes the list of verbs:
    private void createList(String initial) {
        // Hide the bottom layout, AdMob:
        if (MainActivity.isPremium) {
            hideAdMob(true);
        }

        // Clear the previous content of the llListOrQuiz layout:
        LinearLayout ll = (LinearLayout) findViewById(R.id.llListOrQuiz);
        ll.removeAllViews();

        // Create TextViews for each verb:
        int mPaddingDP = MainActivity.mPaddingDP;
        TextView tv;
        Cursor cursor = mDbHelper.queryData("SELECT * FROM verbe WHERE tip LIKE '" + type + "' AND forma1 LIKE '" + initial + "%' ORDER BY forma1");
        cursor.moveToFirst();
        int it = 0; // for number in list.
        String toFormat = getString(R.string.verb_in_list);
        do {
            it++;
            tv = new TextView(this);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
            tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);

            // Format the string:
            String form1 = cursor.getString(1);
            StringBuilder form2 = new StringBuilder(cursor.getString(2));
            StringBuilder form3 = new StringBuilder(cursor.getString(3));
            String translation = cursor.getString(4);

            /*
             * We need to cut the archaic forms if it is not checked,
             * showArchaicForms as false:
             */
            if (!isArchaicForms) {
                // process Form 2:
                String[] forms2 = form2.toString().split("/");
                form2 = new StringBuilder(); // empty for now:
                for (String s : forms2) {
                    if (!s.contains("*")) {
                        form2.append(s).append("/");
                    }
                } // end for.
                form2 = new StringBuilder(form2.substring(0, form2.length() - 1));
                // End process form2.

                // process form 3:
                String[] forms3 = form3.toString().split("/");
                form3 = new StringBuilder(); // empty for now:
                for (String s : forms3) {
                    if (!s.contains("*")) {
                        form3.append(s).append("/");
                    }
                } // end for.
                form3 = new StringBuilder(form3.substring(0, form3.length() - 1));
                // End process form3.
            } // end if is not archaic forms.

            String tvText = String.format(toFormat, "" + it, form1, form2, form3, translation);
            CharSequence tvSeq = MyHtml.fromHtml(tvText);
            tv.setText(tvSeq);
            tv.setFocusable(true);

            // Make a string for be spoken when tapping the text view:
            StringBuilder sb = new StringBuilder();
            sb.append(cursor.getString(1));
            sb.append(", ");
            sb.append(cursor.getString(2));
            sb.append(", ");
            sb.append(cursor.getString(3));
            final String verbForms = sb.toString();

            // No add an listener for short tap:
            tv.setOnClickListener(view -> speak.sayUsingLanguage(verbForms, true));
            // End add listener for tap on verb form.

            // For a long click, spell the result:
            tv.setOnLongClickListener(view -> {
                speak.sayUsingLanguage("", true);
                speak.spellUsingLanguage(verbForms);
                return true;
            });
            // End add listener for long click on a result.

            ll.addView(tv);
        } while (cursor.moveToNext());
    }// end createList() method.

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    } // end onNothingSelected() implemented method.

    // The method to generate the AdMob sequence:
    private void adMobSequence() {
        //initializing the Google Admob SDK
        MobileAds.initialize(this, initializationStatus -> {
            // Now, because it is initialized, we load the ad:
            loadBannerAd();
        });
    } // end adMobSequence().

    // Now we will create a simple method to load the Banner Ad inside QuizActivity class as shown below:
    private void loadBannerAd() {
        // Creating  a Ad Request
        AdRequest adRequest = new AdRequest.Builder().build();
        // load Ad with the Request
        bannerAdView.loadAd(adRequest);
    } // end loadBannerAd() method.
// end Google ads section.

    // A method to hide or show AdMob zone:
    private void hideAdMob(boolean isHide) {
        LinearLayout llBottomInfo = (LinearLayout) findViewById(R.id.llBottomInfo);
        if (isHide) {
            // Hide the llBottomInfo layout:
            llBottomInfo.setVisibility(View.GONE);
        } else {
            llBottomInfo.setVisibility(View.VISIBLE);
        }
    } // end hideAdMob() method.

    // A method which is called when clicking quiz button:
    public void showVerbsQuiz(View view) {
        showVerbsQuizActions();
    } // end showVerbsQuiz|() method.

    // The method to start a new quiz:
    private void showVerbsQuizActions() {
        // At the moment we hide the AdMob zone:
        hideAdMob(true);
        SoundPlayer.playSimple(this, "go_to_right");

        LinearLayout llMainPart = (LinearLayout) findViewById(R.id.llMainPart);
        QuizIrregularVerbs q = new QuizIrregularVerbs(this, llMainPart);
        /* We need btList and btQuiz for enabling in a moment in the class Quiz: */
        q.btList = (Button) findViewById(R.id.btVerbsList);
        q.btQuiz = (Button) findViewById(R.id.btVerbsQuiz);
        q.startQuiz();
    } // end showVerbsQuizActions() method.

    // A method which takes the string for aLastXMarks:
    public static void getLastXMarks(Context context) {
        arLastXMarks.clear();
        Settings set = new Settings(context);
        String lastXMarks = set.getStringSettings("lastXMarks");
        // Only if there is something there:
        if (lastXMarks != null) {
            String[] aLastXMarks = lastXMarks.split("\\|");
            Collections.addAll(arLastXMarks, aLastXMarks);
        } else {
            // If lastXMarks doesn't contains something:
            arLastXMarks.add(String.format(context.getString(R.string.no_last_x_marks), "" + limitForLastMarks));
        }
    } // end getLastXMarks() method.

    // A method to save lastXMarks:
    public static void saveLastXMarks(Context context, double lastMark) {
        getLastXMarks(context);
        // Get the time stamp for last mark insert:
        Calendar cal = Calendar.getInstance();
        int curTime = (int) (cal.getTimeInMillis() / 1000);
        // Format the string to add at index 0 of the ArrayList:
        String temp = "" + lastMark + "-" + curTime;
        // Check if at the index 0 is the no marks message:
        String tempMsg = String.format(context.getString(R.string.no_last_x_marks), "" + limitForLastMarks);
        if (arLastXMarks.get(0).equals(tempMsg)) {
            arLastXMarks.clear();
        }
        arLastXMarks.add(0, temp);

        // Make a big string delimited by pipes:
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < limitForLastMarks && i < arLastXMarks.size(); i++) {
            sb.append(arLastXMarks.get(i));
            // Add the pipe if it is not the last index of the ArrayList:
            if (i < arLastXMarks.size() - 1) {
                sb.append("|");
            }
        } // end for.
        // Save not that string:
        Settings set = new Settings(context);
        set.saveStringSettings("lastXMarks", sb.toString());
    } // end saveLastXMarks() method.

    // This is a subclass:
    private class GetWebData extends AsyncTask<String, String, String> {

        private ProgressDialog pd;

        // execute before task:
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(mFinalContext);
            pd.setMessage(getString(R.string.please_wait));
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
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
            // Clear progress dialog:
            pd.dismiss();
            // Do something with the interface:
            GUITools.changeNickname(mFinalContext, s);
        } // end postExecute() method.
    } // end subclass.

    public void beforeChangeNickname() {
        // Check if we have the Google Account Name:
        if (MainActivity.myAccountName == null) {
            GUITools.alert(this, getString(R.string.warning), getString(R.string.no_account_name_detected_because_permission));
        } // end if account name wasn't detected, permission issue.
        else {
            String url = "https://android.pontes.ro/erd/get_name.php?google_id=" + MainActivity.myAccountName;
            new GetWebData().execute(url);
        } // end if google account name exists.
    } // end beforeChangeNickname() method.

    // Let's see what happens when a check box is clicked:
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        Settings set = new Settings(this); // to save changes.

        // Check which check box was clicked
        if (view.getId() == R.id.cbtDerivedForms) {
            if (checked) {
                isDerivedForms = true;
                type = "%";
            } else {
                isDerivedForms = false;
                type = "0";
            }
            set.saveBooleanSettings("isDerivedForms", isDerivedForms);
        } else if (view.getId() == R.id.cbtArchaicForms) {
            isArchaicForms = checked;
            set.saveBooleanSettings("isArchaicForms", isArchaicForms);
        }

        // Play also a sound:
        SoundPlayer.playSimple(this, "element_clicked");

        // We update the spinner:
        updateWelcomeMessage();
        updateSpinner();
    } // end onClick method .

    /*
     * Some small methods with VIEW at parameter for the simulated action bar in
     * TV layout. This is necessary because we need to click them from layout
     * resource.
     */
    public void goToDictionary(View view) {
        this.finish();
        GUITools.goToDictionary(this);
    } // end goToDictionaryMethod.

    public void goToVocabulary(View view) {
        this.finish();
        GUITools.goToVocabulary(this);
    } // end goToIrregularVerbs() method.

    public void viewStatistics(View view) {
        viewStatistics();
    }

    public void viewOnlineStatistics(View view) {
        GUITools.viewOnlineStatistics(this);
    }

    public void beforeChangeNickname(View view) {
        beforeChangeNickname();
    }

    public void openAppInPlayStore(View view) {
        GUITools.openAppInPlayStore(this);
    }

    public void goToHelp(View view) {
        GUITools.showHelp(this);
    }

    public void goToAbout(View view) {
        GUITools.aboutDialog(this);
    }
    /*
     * End methods to go in different parts from simulated action bar in TV //
     * layout.
     */

} // end VerbsActivity class.
