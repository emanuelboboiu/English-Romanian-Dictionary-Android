package ro.pontes.englishromaniandictionary;

import static com.google.android.gms.common.util.CollectionUtils.listOf;

import android.annotation.TargetApi;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
 * Class started on Friday, 04 September 2015, created by Emanuel Boboiu.
 * This is the main class of this application.
 * */

public class MainActivity extends Activity {

    // The following fields are used for the shake detection:
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    // End fields declaration for shake detector.

    public final static String EXTRA_MESSAGE = "ro.pontes.englishromaniandictonary.MESSAGE";

    private DBAdapter mDbHelper;
    public static boolean isSpeech = true;
    public static boolean isSound = true;
    public static boolean isShake = false;
    public static float onshakeMagnitude = 2.5F;
    public static boolean isWakeLock = true;
    public static boolean isImeAction = true;
    public static boolean isSearchFullText = false;
    public static boolean isHistory = true;
    public static boolean isOrientationBlocked = false;
    public static int textSize = 20; // for TextViews.
    public static String background = null;
    public static int resultsLimit = 200;
    public static int mPaddingDP = 3; // for padding at text views of results.
    public static int direction = 0; // 0 en_ro, 1 ro_en.
    public static String myAccountName = "Anonymous";
    private static final String myUniqueId = "xyzxyzxyz890890890";
    public static boolean isPremium = false;
    private final String mProduct = "erd.premium";
    public static String mUpgradePrice = "�";
    private int idSection = 0;
    public static int numberOfLaunches = 0;
    public static String weSeparator = " � ";
    public static String tempWESeparator = " � ";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    // public String spoken;
    public static boolean isTV = false;
    public static int langNumber = 0;
    private static int tempLang = 0;

    private final Context mFinalContext = this;

    private StringTools st;
    private SpeakText speak;
    private SpeakText2 speak2;

    // A static variable to have for LexicalResources class:
    public static String lastStringInSearchEdit = "";

    private SearchHistory searchHistory;

    private String[] aDirection; // for text edit hint.
    private String[] aSpeechDirection; // for recognise prompt. edit.

    private int searchedWords = 0; // increment to post the number.
    private int spokenWords = 0; // increment to post the number.
    private int spokenExplanations = 0; // increment to post the number.
    private int spelledWords = 0; // increment to post the number.

    // Controls used globally in the application:
    LinearLayout llResults; // for central part of the activity.
    private LinearLayout llBottomInfo = null;
    private LinearLayout llStatusAndImageButtons = null;
    private TextView tvStatus = null;
    /*
     * The height of the more about a word imageButton will be the height of a
     * normal text view, the height of the tvStatus:
     */
    private int ibMoreHeight = 0;
    private int llResultsWidth = 0;

    /*
     * We need a global variable TextView for of a result. A value will be
     * attributed in onCreateContextMenu, and will be read in
     * onContextItemSelected:
     */
    private TextView tvResultForContext;

    /*
     * We need two variables to store the last word and explanation clicked
     * longly for context menu:
     */
    private String lastCMW;
    private String lastCME;

    // For billing:
    private PurchasesUpdatedListener purchasesUpdatedListener;
    private BillingClient billingClient;
    List<ProductDetails> myProducts;

    // Creating object of AdView:
    private AdView bannerAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Charge settings:
        Settings set = new Settings(this);
        set.chargeSettings();

        // We charge if needed the language chosen, if is not default:
        setLocale(langNumber);

        // Determine if it's a TV or not:
        if (GUITools.isAndroidTV(this)) {
            isTV = true;
            // We don't need ads ads on TV yet, we make it premium version for an undetermined while:
            isPremium = true;
        } else {
            isTV = false;
        } // end determine if it's Android TV.

        /*
         * We charge different layouts depending of the premium status or
         * android TV:
         */
        if (isTV) {
            if (isPremium) {
                setContentView(R.layout.activity_main_premium_tv);
            } else {
                setContentView(R.layout.activity_main_tv);
            }
        } else {
            // No if is not a TV:
            if (isPremium) {
                setContentView(R.layout.activity_main_premium);
            } else {
                setContentView(R.layout.activity_main);
            }

            // Set the orientation to be portrait if is blocked in settings:
            if (isOrientationBlocked) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } // end if isOrientationBlocked is true.
        }
        // end charging the correct layout.

        // Calculate the pixels in DP for mPaddingDP, for TextViews of the
        // results:
        int paddingPixel = 3;
        float density = getResources().getDisplayMetrics().density;
        mPaddingDP = (int) (paddingPixel * density);
        // end calculate mPaddingDP

        // To keep screen awake:
        if (MainActivity.isWakeLock) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } // end wake lock.

        // Start things for our database:
        mDbHelper = new DBAdapter(this);
        mDbHelper.createDatabase();
        mDbHelper.open();

        Resources res = getResources();
        aDirection = res.getStringArray(R.array.direction_array);
        aSpeechDirection = res.getStringArray(R.array.speech_direction_array);

        // Find the llResults:
        llResults = findViewById(R.id.llResults);

        // Charge the bottom linear layout:
        llBottomInfo = findViewById(R.id.llBottomInfo);

        // Charge the layout with status and update buttons:
        llStatusAndImageButtons = findViewById(R.id.llStatusAndImageButtons);

        // Find the status TextView to have it also globally:
        tvStatus = findViewById(R.id.tvStatus);

        speak = new SpeakText(this); // For English.
        speak2 = new SpeakText2(this); // for Romanian.

        searchHistory = new SearchHistory(this);

        // Other things at onCreate:
        // a method found in this class.
        updateGUIFirst();

        // ShakeDetector initialisation:
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setShakeThresholdGravity(MainActivity.onshakeMagnitude);
        /*
         * Method you would use to setup whatever you want done once the
         * device has been shook.
         */
        mShakeDetector.setOnShakeListener(this::handleShakeEvent);
        // End initialisation of the shake detector.

        if (!isTV) {
            GUITools.checkIfRated(this);
            GUITools.checkIfNoticedAboutPremium(this);
        } // end if is not Android TV.

        // Some lines for detecting if search is from history or vocabulary
        // regions:
        String historyMessage = getIntent().getStringExtra("wordFromHistory");
        if (historyMessage != null && !historyMessage.isEmpty()) {
            int historyDirection = Integer.parseInt(historyMessage.substring(historyMessage.length() - 1));
            String historyWord = historyMessage.substring(0, historyMessage.length() - 1);
            searchFromHistory(historyWord, historyDirection);
        }
        // end search from history or vocabulary via intent.

        // To determine the tvStatusHeight and the llResults width:
        determineSomeSizes();

        if (!isPremium) {
            // For billing:
            startBillingDependencies();
            bannerAdView = findViewById(R.id.bannerAdView);
            adMobSequence();
        }
    } // end onCreate() method.

    private void determineSomeSizes() {
        /*
         * Determine the height of the tvStatus, this height will be the height
         * and the width of the more ImageButton for each result:
         */
        llBottomInfo.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // Only if it is not already determined:
            if (ibMoreHeight == 0) {
                TextView tv = findViewById(R.id.tvStatus);
                ibMoreHeight = tv.getHeight();
            } // end if it was not determined.
        });
        // End determine the height of the tvStatus.

        llResults.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // Only if it is not already determined:
            if (llResultsWidth == 0) {
                llResultsWidth = llResults.getWidth();

            } // end if llResultsWidth was not determined.
        });
        // End determine the width of the llResults.
    }// end determineSomeSizes() method.

    //
    @Override
    public void onResume() {
        super.onResume();

        // Some initial things like background:
        GUITools.setLayoutInitial(this, 1);

        /*
         * We need here StringTools object because some time the keyboard can be
         * changed without recreating the activity:
         */
        st = new StringTools(this);

        if (MainActivity.isShake) {
            /*
             * Add the following line to register the Session Manager Listener
             * onResume:
             */
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    } // end onResume method.

    @Override
    public void onPause() {
        // Add here what you want to happens on pause:
        speak.stop();
        speak2.stop();
        postStatistics();
        if (MainActivity.isShake) {
            // Add the following line to unregister the Sensor Manager onPause:
            mSensorManager.unregisterListener(mShakeDetector);
        }
        super.onPause();
    } // end onPause method.

    @Override
    protected void onDestroy() {
        // Close the database connection:
        mDbHelper.close();

        // Shut down also the TTS:
        speak.close();
        speak2.close();
        super.onDestroy();
    } // end onDestroy method.

    @Override
    public void onBackPressed() {
        this.finish();
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    } // end onBackPressed()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    } // end onCreateOptionsMenu() method.

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.mnuActionSettings) {
            goToSettings();
        } else if (id == R.id.mnuTTSSettings) {
            goToTTSSettings();
        } else if (id == R.id.mnuSeparatorSettings) {
            chooseSeparator();
        } else if (id == R.id.mnuDisplaySettings) {
            goToDisplaySettings();
        } else if (id == R.id.mnuBackgroundSettings) {
            goToBackgroundSettings();
        } else if (id == R.id.mnuChooseLanguageSettings) {
            chooseLanguage();
        } else if (id == R.id.mnuIrregularVerbs) {
            GUITools.goToIrregularVerbs(this);
        } else if (id == R.id.mnuVocabulary) {
            GUITools.goToVocabulary(this);
        } else if (id == R.id.mnuSearchHistory) {
            GUITools.goToHistory(this);
        } else if (id == R.id.mnuUpdateNewWords) {
            updateNowNewWords();
        } else if (id == R.id.mnuProposeNewWords) {
            proposeNowNewWords();
        } else if (id == R.id.mnuGoToWebPage) {
            GUITools.goToAppWebPage(this);
        } else if (id == R.id.mnuViewOnlineStatistics) {
            GUITools.viewOnlineStatistics(this);
        } else if (id == R.id.mnuAppInPlayStore) {
            GUITools.openAppInPlayStore(this);

        } else if (id == R.id.mnuGetPremiumVersion) {
            upgradeAlert();
        } // end if upgrade to premium version was pressed.
        else if (id == R.id.mnuResetDefaults) {
            resetToDefaults();
        } else if (id == R.id.mnuHelp) {
            GUITools.showHelp(this);
        } // end if Help is chosen in main menu.
        else if (id == R.id.mnuAboutDialog) {
            GUITools.aboutDialog(this);
        } // end if about game is chosen in main menu.
        else if (id == R.id.mnuRate) {
            GUITools.showRateDialog(this);
        } // end if rate option was chosen in menu.

        return super.onOptionsItemSelected(item);
    } // end onOptionsItemSelected() method.

    // The implementations for context menu:
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // We store globally the text view clicked longly:
        tvResultForContext = (TextView) v;
        // First we take the text from the longly clicked result:
        String result = tvResultForContext.getText().toString();
        String[] aResult = result.split(MyHtml.fromHtml(MainActivity.weSeparator).toString());
        // Next two variables are declared globally:
        lastCMW = st.cleanString(aResult[0]);
        lastCME = st.cleanString(aResult[1]);

        String cmTitle = String.format(getString(R.string.cm_result_title), lastCMW);
        menu.setHeaderTitle(cmTitle);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.results_context_menu, menu);
    }

    // A method for the context menu, options chosen there:
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String w = lastCMW;
        String e = lastCME;
        @SuppressWarnings("unused") AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getItemId() == R.id.cmSpeakResult) {
            speakResult(w, e);
            return true;
        } else if (item.getItemId() == R.id.cmSpeakExplanation) {
            speakExplanation(w, e);
            return true;
        } else if (item.getItemId() == R.id.cmSpellResult) {
            spellResult(w, e);
            return true;
        } else if (item.getItemId() == R.id.cmIPAResult) {
            getExternalResource(1, w, e);
            return true;
        } else if (item.getItemId() == R.id.cmWordFrequencyResult) {
            getExternalResource(7, w, e);
            return true;
        } else if (item.getItemId() == R.id.cmSynonymsResult) {
            getExternalResource(2, w, e);
            return true;
        } else if (item.getItemId() == R.id.cmAntonymsResult) {
            getExternalResource(3, w, e);
            return true;
        } else if (item.getItemId() == R.id.cmHomophonesResult) {
            getExternalResource(4, w, e);
            return true;
        } else if (item.getItemId() == R.id.cmHypernymsResult) {
            getExternalResource(8, w, e);
            return true;
        } else if (item.getItemId() == R.id.cmHyponymsResult) {
            getExternalResource(9, w, e);
            return true;
        } else if (item.getItemId() == R.id.cmRhymesResult) {
            getExternalResource(5, w, e);
            return true;
        } else if (item.getItemId() == R.id.cmDefinitionResult) {
            getExternalResource(6, w, e);
            return true;
        } else if (item.getItemId() == R.id.cmFollowersResult) {
            getExternalResource(10, w, e);
            return true;
        } else if (item.getItemId() == R.id.cmPredecessorsResult) {
            getExternalResource(11, w, e);
            return true;
        } else if (item.getItemId() == R.id.cmCopyResult) {
            GUITools.copyIntoClipboard(this, tvResultForContext.getText().toString());
            return true;
        } else if (item.getItemId() == R.id.cmCopyWord) {
            GUITools.copyIntoClipboard(this, w);
            return true;
        } else if (item.getItemId() == R.id.cmCopyExplanation) {
            GUITools.copyIntoClipboard(this, e);
            return true;
        } else if (item.getItemId() == R.id.cmCancelSearch) {
            cancelSearchActions(0);
            return true;
        } else if (item.getItemId() == R.id.cmAddToVocabularyResult) {
            addToVocabulary(w, e);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    } // End context menu implementation.

    // Some methods to go to other activities from menu:
    private void goToSettings() {
        // Called when the user clicks the settings option in menu:
        Intent intent = new Intent(this, SettingsActivity.class);
        String message;
        message = "English Dictionary"; // without a reason.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end go to settings method.

    private void goToTTSSettings() {
        // Called when the user clicks the Voice settings option in menu:
        Intent intent = new Intent(this, TTSSettingsActivity.class);
        String message;
        message = "English Dictionary"; // without a reason.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end go to display settings method.

    private void goToDisplaySettings() {
        // Called when the user clicks the display settings option in menu:
        Intent intent = new Intent(this, DisplaySettingsActivity.class);
        String message;
        message = "English Dictionary"; // without a reason, just to be
        // something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end go to display settings method.

    private void goToBackgroundSettings() {
        // Called when the user clicks the background settings option in menu:
        Intent intent = new Intent(this, BackgroundActivity.class);
        String message;
        message = "English Dictionary"; // without a reason.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end go to background settings method.

    // End methods to go in other activities from menu.

    // A method which is called when shaking the device:
    private void handleShakeEvent(int count) {
        cancelSearchActions(0);
    } // end method for actions on shake.

    // A method to update some text views or other GUI elements at onCreate():
    private void updateGUIFirst() {
        // To have correct the direction as message above search edit:
        updateSearchMessage();

        updateNumberOfWords();

        // Add an action listener for the keyboard:
        EditText input = findViewById(R.id.etWord);
        input.setInputType(input.getInputType() | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchDirectlyFromKeyboard();
            }
            return false;
        });
        // End add action listener.
    } // end updateGUIFIrst() method.

    public void updateNumberOfWords() {
        // The number of words in DB:
        String sql = "SELECT COUNT(*) FROM dictionar" + direction;
        Cursor cursor = mDbHelper.queryData(sql);
        int totalWords = cursor.getInt(0);
        cursor.close();
        // First take the corresponding plural resource:
        Resources res = getResources();
        String numberOfWordsMessage = res.getQuantityString(R.plurals.tv_number_of_words, totalWords, totalWords);

        // Update the tvStatus TextView:
        tvStatus.setText(numberOfWordsMessage);
    } // end updateNumberOfWords() method.

    /*
     * A method to add in the middle of the central layout the buttons for going
     * to my vocabulary and irregular verbs:
     */
    @SuppressWarnings("unused")
    private void addLayoutForModulesButtons() {

        // A LinearLayout for my vocabulary and irregular verbs button:
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER_HORIZONTAL);

        // A LayoutParams to add the buttons in LinearLayout:
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        // Create the my vocabulary button:
        Button bt = new Button(this);
        bt.setText(getString(R.string.bt_my_vocabulary));
        bt.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        // Add the listener:
        bt.setOnClickListener(view -> GUITools.goToVocabulary(mFinalContext));
        // End add listener for tap on button learn.
        // Add now the button:
        ll.addView(bt, lp);

        // Create the irregular verbs button:
        bt = new Button(this);
        bt.setText(getString(R.string.bt_learn_irregular_verbs));
        bt.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        // Add the listener:
        bt.setOnClickListener(view -> GUITools.goToIrregularVerbs(mFinalContext));
        // End add listener for tap on button learn.
        // Add now the button:
        ll.addView(bt, lp);

        // A LayoutParams for LinearLayout to add in llResults:
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        llp.setMargins(0, mPaddingDP * 7, 0, 0);
        // Add it now:
        llResults.addView(ll, llp);
    } // end addLayoutForModulesButtons() method.

    // Methods for buttons when searching:
    public void searchButton(View view) {
        getWordFromDB(direction);
    } // end searchButton() method.

    public void searchDirectlyFromKeyboard() {
        if (isImeAction) {
            getWordFromDB(direction);
        }
    } // end search directly from keyboard.

    // A method to get the text filled in the search EditText:
    private String getTextFromEditText() {
        EditText input = findViewById(R.id.etWord);
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
        String text = input.getText().toString();
        // Check if there is something typed there:
        if (text.length() < 2) {
            // Show a warning here if written text is shorter than 2 characters:
            GUITools.alert(this, getString(R.string.warning), getString(R.string.warning_wrong_search));
            SoundPlayer.playSimple(this, "results_not_available");

            return null;
        } else {
            // Increase the number of searches:
            searchedWords = searchedWords + 1;
            if (!isPremium) {
                // We check if is a manual registration:
                if (text.equalsIgnoreCase(myUniqueId)) {
                    recreateThisActivityAfterRegistering();
                    return null;
                } // end if is a manual registration.
            } // end if is not premium.

            return st.replaceCharacters(text);
        }
    } // end getTextFromEditText() method.

    // The method to search and show a query:
    private void getWordFromDB(int direction) {
        // Get the string filled in the EditText:
        String word = getTextFromEditText();
        lastStringInSearchEdit = word;

        // Only if there is something typed in the EditText:
        if (word != null) {
            // Make now the query string depending if is search middle or
            // not:
            String SQL;

            // Make the SQL query string depending of the search type:
            if (isSearchFullText) {
                SQL = "SELECT *, 1 AS sortare FROM dictionar" + direction + " WHERE termen='" + word + "' OR termen LIKE '" + word + ",%' OR termen LIKE '" + word + ".%' OR termen LIKE '" + word + " %' UNION SELECT *, 2 AS sortare FROM dictionar" + direction + " WHERE termen LIKE '% " + word + "' OR termen LIKE '% " + word + ",%' OR termen LIKE '% " + word + ".%' OR termen LIKE '% " + word + " %' ORDER BY sortare, termen";
            } else {
                // No full text search:
                SQL = "SELECT *, 1 AS sortare from dictionar" + direction + " WHERE termen LIKE '" + word + "%' union SELECT *,2 AS sortare from dictionar" + direction + " WHERE termen LIKE '%" + word + "%' AND termen NOT LIKE '" + word + "%' ORDER BY sortare, termen";
            } // end SQL query string for not full text.

            Cursor cursor = mDbHelper.queryData(SQL);
            int type = 0; // word not found.
            // Only if there are results:
            int count = cursor.getCount();
            if (count > 0) {
                type = 1; // word found.
                // Play a specific sound for results shown:
                SoundPlayer.playSimple(this, "results_shown");

                // Hide the llBottomInfo layout if is premium version or TV:
                if (isPremium || isTV) {
                    llBottomInfo.setVisibility(View.GONE);
                } // end if is premium or TV versions.
                else {
                    /*
                     * If is not premium or TV we eliminate the status TextView
                     * and image buttons from llStatusAndImageButtons
                     * LinearLayout. This way we have more space for results.
                     * Only the ads will remain at the bottom of the screen.
                     */
                    llStatusAndImageButtons.setVisibility(View.GONE);
                } // end if is not premium or TV.

                // Clear the previous content of the llResult layout:
                llResults.removeAllViews();

                /*
                 * Create a text view for title, announcing the number of
                 * results:
                 */
                // First take the corresponding plural resource:
                Resources res = getResources();
                String foundResults = res.getQuantityString(R.plurals.tv_number_of_results, count, count);
                // Create the number of results text view:
                TextView tvResults = new TextView(this);
                tvResults.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize + 1);
                tvResults.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                tvResults.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
                tvResults.setText(foundResults);
                tvResults.setId(R.id.tvNumberOfResults);
                tvResults.setFocusable(true);
                tvResults.setNextFocusUpId(R.id.btCancelSearch);
                int idResultsStart = 2000000000; // here starts results as IDs.
                // Difference between ID of a TV and its more IB correspondent:
                int idDifference = resultsLimit * 2;
                tvResults.setNextFocusDownId(idResultsStart + 1);
                llResults.addView(tvResults);

                /*
                 * Create TextViews for each word and put them in a horizontal
                 * linear layout to have also a More options button.
                 */
                // We need the string with place holders:
                String tvWordAndExplanation = getString(R.string.tv_word_and_explanation);
                /*
                 * For limit, we have a variable which will be incremented until
                 * resultsLimit:
                 */
                int it = 0;
                int curResultId = idResultsStart + 1;
                LinearLayout llOneResult;
                // We need also a LayoutParams for this linear layout:
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

                // We need also a LayoutParams for each text view:
                /* The width of a TV is the llResults width minus ibMoreHeight: */
                int tvWidth = llResultsWidth - ibMoreHeight;
                LinearLayout.LayoutParams lpChild1 = new LinearLayout.LayoutParams(tvWidth, LayoutParams.WRAP_CONTENT);

                // We need also a LayoutParams for each more options button:
                LinearLayout.LayoutParams lpChild2 = new LinearLayout.LayoutParams(ibMoreHeight, ibMoreHeight);

                final TextView[] tv = new TextView[resultsLimit + 1];
                cursor.moveToFirst();
                do {
                    llOneResult = new LinearLayout(this);
                    llOneResult.setOrientation(LinearLayout.HORIZONTAL);
                    llOneResult.setGravity(Gravity.CENTER_VERTICAL);

                    tv[it] = new TextView(this);
                    tv[it].setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                    tv[it].setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);

                    // w means word, e means explanation:
                    final String w = cursor.getString(0);
                    final String e = cursor.getString(1);
                    String tvText = String.format(tvWordAndExplanation, w, weSeparator, e);
                    CharSequence tvSeq = MyHtml.fromHtml(tvText);
                    tv[it].setText(tvSeq);
                    tv[it].setId(curResultId);
                    if (it > 0) {
                        tv[it].setNextFocusUpId(curResultId - 1);
                    } else {
                        tv[it].setNextFocusUpId(R.id.tvNumberOfResults);
                    }
                    tv[it].setNextFocusRightId(curResultId + idDifference);
                    tv[it].setNextFocusDownId(++curResultId);
                    tv[it].setFocusable(true);

                    // For a short click, speak result:
                    tv[it].setOnClickListener(view -> speakResult(w, e));
                    // End add listener for short click on a result.

                    registerForContextMenu(tv[it]);

                    llOneResult.addView(tv[it], lpChild1);

                    // Create also the ImageButton for more options:
                    ImageButton ib = new ImageButton(this);
                    ib.setImageResource(android.R.drawable.ic_menu_more);
                    ib.setBackgroundResource(R.drawable.selector_background_selected);
                    ib.setContentDescription(String.format(getString(R.string.ib_more_for_results), w));

                    ib.setId(curResultId + idDifference - 1); // it was already
                    // incremented.
                    if (it > 0) {
                        ib.setNextFocusUpId(curResultId + idDifference - 2);
                    } else {
                        ib.setNextFocusUpId(R.id.tvNumberOfResults);
                    }
                    ib.setNextFocusLeftId(curResultId - 1);
                    ib.setNextFocusDownId(curResultId + idDifference);

                    final int itf = it;
                    // For a short click, show context menu:
                    ib.setOnClickListener(view -> tv[itf].showContextMenu());
                    // End add listener for short click on a result.

                    // A long click for more options:
                    ib.setOnLongClickListener(view -> {
                        LexicalResources lr = new LexicalResources(mFinalContext);
                        lr.getExternalResource(1, w, e);
                        return true;
                    });
                    // End add listener for long click on more button.

                    llOneResult.addView(ib, lpChild2);

                    llResults.addView(llOneResult, lp);

                    it++; // increase for limit.
                    if (it >= resultsLimit) {
                        break;
                    }
                } while (cursor.moveToNext());
                // end do ... while.

                // Show the message for more than limit results:
                if (count > resultsLimit) {
                    String moreResultsMessage = String.format(getString(R.string.message_for_more_results), "" + resultsLimit);
                    tv[it] = new TextView(this);
                    tv[it].setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                    tv[it].setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
                    tv[it].setPadding(mPaddingDP, mPaddingDP * 2, mPaddingDP, mPaddingDP);
                    tv[it].setText(moreResultsMessage);
                    tv[it].setNextFocusUpId(curResultId - 1);
                    tv[it].setId(curResultId);
                    tv[it].setFocusable(true);
                    llResults.addView(tv[it]);
                } // end show message for more results.
            } // end if there were results in cursor.
            // If there are no results, getCount is 0:
            else {
                showWhenNoResults(word);
            } // end if there were no results.

            // Insert last search into database:
            if (isHistory) {
                searchHistory.addRecord(word, direction, type);
            } // end if search history is activated.
        } // end if there was something typed in the EditText.
    } // end getWordFromDB() method.

    // A method to cancel a search:
    public void cancelSearchButton(View view) {
        cancelSearchActions(0);
    } // end cancelButton method.

    private void cancelSearchActions(int where) {
        // Find the edit text to erase all content:
        EditText et = findViewById(R.id.etWord);
        et.setText("");

        // Show the keyboard for a new search:
        if (where == 0) {
            SoundPlayer.playSimple(this, "results_canceled");
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        } // end if is from cancelButton or shake action.

        // Erase also the llResults layout:
        llResults.removeAllViews();

        // Show again the llBottomInfo layout if is premium or TV:
        if (isPremium || isTV) {
            llBottomInfo.setVisibility(View.VISIBLE);
        } // end if is premium or TV.
        else {
            /*
             * Show again the tvStatus number of words in DB and update buttons
             * from llStatusAndImageButtons if is not premium and not TV:
             */
            llStatusAndImageButtons.setVisibility(View.VISIBLE);
        }

        // Add the button for irregular verbs:
        // addLayoutForModulesButtons();
    } // end cancelSearchActions method.

    // A method to write in the results area that there are no results:
    private void showWhenNoResults(String searchedWord) {

        // Play a corresponding sound if results are not available:
        SoundPlayer.playSimple(this, "results_not_available");

        // Clear the previous content of the llResult layout:
        llResults.removeAllViews();

        // Create a TextView for message no results:
        TextView tv = new TextView(this);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
        searchedWord = st.polishString(searchedWord);
        String tvText = String.format(getString(R.string.warning_not_results), searchedWord);
        CharSequence tvSeq = MyHtml.fromHtml(tvText);
        tv.setText(tvSeq);
        tv.setId(R.id.tvNumberOfResults);
        tv.setFocusable(true);
        tv.setNextFocusUpId(R.id.etWord);
        tv.setNextFocusDownId(R.id.tvStatus);
        llResults.addView(tv);
    } // end showWhenNoResults method.

    // A method for switch button:
    public void switchButton(View view) {
        switchDirectionAction();
    } // end switchButton() method.

    // The method which makes the switch:
    private void switchDirectionAction() {
        postStatistics();
        SoundPlayer.playSimple(this, "switch_direction");
        if (direction == 1) {
            // It means it is Romanian English, it will be English Romanian:
            direction = 0;
        } else {
            direction = 1;
        }
        updateGUIFirst();

        // Save the new direction in SharedPreferences:
        Settings set = new Settings(this);
        set.saveIntSettings("direction", direction);
    } // end switchDirectionAction() method.

    // The method which updates the text view for search message:
    private void updateSearchMessage() {
        EditText et = findViewById(R.id.etWord);
        et.setHint(aDirection[direction]);

        // Set also the image with flags for switch button:
        String flagFileName = "flag" + direction;
        ImageView ib = findViewById(R.id.btSwitch);
        String uri = "@drawable/" + flagFileName;
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        ib.setImageResource(imageResource);

        cancelSearchActions(1);
    } // end updateSearchMessage() method.

    // A method to post statistics:
    private void postStatistics() {
        // Post the statistics and return:
        if (searchedWords > 0) {
            int statsTip = 26 + direction;
            Statistics stats = new Statistics(this);
            stats.postStats("" + statsTip, searchedWords);
            searchedWords = 0;
        } // end post number of searched words.

        // Post number of spoken words:
        if (spokenWords > 0) {
            Statistics stats = new Statistics(this);
            stats.postStats("76", spokenWords);
            spokenWords = 0;
        } // end if there are spoken words for statistics.

        // Post number of spoken explanations:
        if (spokenExplanations > 0) {
            Statistics stats = new Statistics(this);
            stats.postStats("78", spokenExplanations);
            spokenExplanations = 0;
        } // end if there are spoken explanations for statistics.

        // Post number of spelled words:
        if (spelledWords > 0) {
            Statistics stats = new Statistics(this);
            stats.postStats("77", spelledWords);
            spelledWords = 0;
        } // end if there are spelled words for statistics.
    } // end postStatistics() method.

    // A method to speak text from a line of results:
    private void speakResult(final String word, final String explanation) {
        // If is English - Romanian direction::
        if (direction == 0) {
            speak.sayUsingLanguage(word, true);
        } // end if is English Romanian variant.
        else { // Romanian version:
            speak2.sayUsingLanguage(word, true);
        } // end if is Romanian English variant.
        spokenWords++; // for statistics.
    } // end speakResult() method.

    // A method to speak text from explanation:
    private void speakExplanation(final String word, final String explanation) {
        // If is English - Romanian direction::
        if (direction == 0) {
            speak2.sayUsingLanguage(explanation, true);
        } // end if is English Romanian variant.
        else { // Romanian version:
            speak.sayUsingLanguage(explanation, true);
        } // end if is Romanian English variant.
        spokenExplanations++; // for statistics.
    } // end speakExplanation() method.

    // A method to spell text from a line of results:
    private void spellResult(final String word, final String explanation) {
        // If is English - Romanian version::
        if (direction == 0) {
            speak.spellUsingLanguage(word);
        } // end if is English Romanian variant.
        else { // Romanian - English direction:
            speak2.spellUsingLanguage(word);
        } // end if is Romanian English variant.
        spelledWords++;
    } // end speakResult() method.


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

    // The finishing of the speak:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If it's here after speech:
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String spoken = result.get(0);
                // Now set the etWord:
                EditText et = findViewById(R.id.etWord);
                et.setText(spoken);
                getWordFromDB(direction);
            }
        } // end if it's here after a speech.
    } // end onActivityResult() method.

    // A method which recreates this activity:
    private void recreateThisActivity() {
        finish();
        startActivity(getIntent());
    } // end recreateThisActivity() method.

    // Methods for add to vocabulary:
    private void addToVocabulary(final String word, final String explanation) {
        // Start things for our database vocabulary:
        final DBAdapter2 mDbHelper2 = new DBAdapter2(this);
        mDbHelper2.createDatabase();
        mDbHelper2.open();

        // Reset some values:
        idSection = 0;

        // Make a new context:
        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);

        // We create first a layout for this action:
        LinearLayout addLLMain = new LinearLayout(context);
        addLLMain.setOrientation(LinearLayout.VERTICAL);

        // A LayoutParams to add next items into addLLMain:
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView tvAddThis = new TextView(context);
        tvAddThis.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        tvAddThis.setPadding(0, mPaddingDP, 0, mPaddingDP);
        // Make the string for this text view:
        String strAddThis = String.format(getString(R.string.tv_add_this_word), String.format(getString(R.string.tv_word_and_explanation), word, weSeparator, explanation));
        tvAddThis.setText(MyHtml.fromHtml(strAddThis));
        tvAddThis.setFocusable(true);
        addLLMain.addView(tvAddThis, llParams);

        // A radio group for categories:
        final RadioGroup rg = new RadioGroup(context);

        // We need to make here an edit text and check boxes for each category:
        // In categories check boxes will appear also the number of existing
        // words:
        final EditText et = new EditText(context);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        et.setHint(getString(R.string.et_new_vocabulary_hint));
        et.setPadding(mPaddingDP, mPaddingDP * 3, mPaddingDP, mPaddingDP);
        et.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // We uncheck all radio buttons:
                rg.clearCheck();
            } // end if action done was chosen.
            return false;
        });
        // End add action listener for the IME done button of the keyboard..
        et.setFocusable(true);
        addLLMain.addView(et, llParams);

        // In scroll view all the existing categories as check boxes:
        ScrollView sv = new ScrollView(context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        // Now create the sections as radio buttons:
        // First query the existing sections:
        String sql = "SELECT * FROM sectiuni ORDER BY nume COLLATE NOCASE";
        final Cursor cursorSections = mDbHelper2.queryData(sql);

        // A do ... while to create the radio buttons:
        int count = cursorSections.getCount();
        if (count > 0) {
            cursorSections.moveToFirst();
            // Make a RadioGroup:

            rg.setOrientation(RadioGroup.VERTICAL);
            // A LayoutParams to add as match parent the radio buttons:
            RadioGroup.LayoutParams rgParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            Resources res = getResources();
            do {
                RadioButton rbt = new RadioButton(context);
                rbt.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                rbt.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
                // Set the catTitle:
                final int curSection = cursorSections.getInt(0);
                int nrOfWords = getNumberOfWordsInCategory(curSection);
                String catTitle = String.format(getString(R.string.cbt_category), cursorSections.getString(1), res.getQuantityString(R.plurals.number_of_words_in_category, nrOfWords, nrOfWords));
                rbt.setText(MyHtml.fromHtml(catTitle));

                rbt.setOnClickListener(view -> {
                    // Change the id of the chosen section:
                    idSection = curSection;
                    // Empty the edit text because a section was chosen:
                    et.setText("");
                });
                // End add listener for tap on radio button.
                rg.addView(rbt, rgParams);
            } while (cursorSections.moveToNext());
            ll.addView(rg);
        } // end if there are sections.

        sv.addView(ll);
        addLLMain.addView(sv);

        // Create now the alert:
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(getString(R.string.add_to_vocabulary_alert_title));
        alertDialog.setView(addLLMain);

        // The buttons can be add now and cancel!:
        // end if add now was pressed.
        alertDialog.setPositiveButton(getString(R.string.bt_add_now), (dialog, whichButton) -> {
            // Start the add process:

            // We need the time in seconds:
            final long timeInSeconds = GUITools.getTimeInSeconds();

            /*
             * If there is written something in the edit text, we
             * consider a new category:
             */
            String etText = et.getText().toString();
            if (etText.length() > 1 && !(etText.equals(" ") || etText.equals("  ") || etText.equals("   "))) {
                etText = st.realEscapeString(etText);
                // Check if section doesn't already exist:
                if (!fieldExists("sectiuni", "nume", etText)) {
                    String sql1 = "INSERT INTO sectiuni (nume, descriere, data) VALUES ('" + etText + "', 'none', '" + timeInSeconds + "');";
                    mDbHelper2.insertData(sql1);

                    /*
                     * Post a record into DB Statistics about
                     * section creation:
                     */
                    Statistics stats = new Statistics(mFinalContext);
                    stats.postStats("29", 1);

                } // end if section name doesn't already exists.
                else {
                    GUITools.alert(mFinalContext, getString(R.string.warning), getString(R.string.this_vocabulary_section_already_exists));
                } // end if section name exists.
                /*
                 * After we created this new section, we must
                 * extract the idSection of this:
                 */
                String sql1 = "SELECT id FROM sectiuni WHERE nume='" + etText + "'";
                Cursor tempCursor = mDbHelper2.queryData(sql1);
                idSection = tempCursor.getInt(0);
            } // end if etText was not empty.

            /*
             * Check now if idSection is greater than 0. It means a
             * section was written or it was chosen:
             */
            if (idSection > 0) {

                // Add the word and explanation effectively if
                // record doesn't exists:
                if (!recordExistsInVocabulary(st.realEscapeString(word), st.realEscapeString(explanation))) {
                    String sql1 = "INSERT INTO vocabular (idSectiune, termen, explicatie, data, tip) VALUES ('" + idSection + "', '" + st.realEscapeString(word) + "', '" + st.realEscapeString(explanation) + "', '" + timeInSeconds + "', '" + direction + "')";
                    mDbHelper2.insertData(sql1);
                    SoundPlayer.playSimple(mFinalContext, "hand_writting");

                    /*
                     * Post in statistics about this insert of a
                     * word in DB:
                     */
                    Statistics stats = new Statistics(mFinalContext);
                    stats.postStats("30", 1);

                } // end if record doesn't exist, the best scenario.
                else {
                    GUITools.alert(mFinalContext, getString(R.string.warning), getString(R.string.this_record_already_exists_in_vocabulary));
                } // end if record already exist.
            } else {
                // No section was chosen or written:
                GUITools.alert(mFinalContext, getString(R.string.warning), getString(R.string.no_category_chosen));
            } // end if idSection isn't greater than 0.
            mDbHelper2.close();
        });

        alertDialog.setNegativeButton(getString(R.string.bt_cancel), (dialog, whichButton) -> {
            // Cancelled:
            mDbHelper2.close();
        });

        alertDialog.create();
        alertDialog.show();

    } // end addToVocabulary() method.

    // A method which checks if this entry already exists in DB:
    private boolean recordExistsInVocabulary(String word, String explanation) {
        // This method exists also in VocabularyActivity.
        boolean exists = false;
        DBAdapter2 mDbHelperTemp = new DBAdapter2(this);
        mDbHelperTemp.createDatabase();
        mDbHelperTemp.open();

        String sql = "SELECT COUNT(*) AS total FROM vocabular WHERE termen='" + word + "' AND explicatie='" + explanation + "'";
        Cursor cur = mDbHelperTemp.queryData(sql);
        int count = cur.getInt(0);
        cur.close();
        mDbHelperTemp.close();

        if (count > 0) {
            exists = true;
        }

        return exists;
    } // end recordExists() method.

    // A method which check if a field exists in a database:
    private boolean fieldExists(String table, String field, String text) {
        boolean exists = false;
        DBAdapter2 mDbHelperTemp = new DBAdapter2(this);
        mDbHelperTemp.createDatabase();
        mDbHelperTemp.open();

        String sql = "SELECT COUNT(*) AS total FROM " + table + " WHERE " + field + " = '" + text + "';";
        Cursor cur = mDbHelperTemp.queryData(sql);
        int count = cur.getInt(0);
        cur.close();
        mDbHelperTemp.close();

        if (count > 0) {
            exists = true;
        }

        return exists;
    } // end fieldExists() method.

    // A method which gets the number of words in a category:
    private int getNumberOfWordsInCategory(int id) {
        DBAdapter2 mDbHelperTemp = new DBAdapter2(this);
        mDbHelperTemp.createDatabase();
        mDbHelperTemp.open();

        String sql = "SELECT COUNT(*) AS total FROM vocabular WHERE idSectiune = '" + id + "';";
        Cursor cur = mDbHelperTemp.queryData(sql);
        int count = cur.getInt(0);
        cur.close();
        mDbHelperTemp.close();

        return count;
    } // end getNumberOfWordsInCategory() method.
    // end methods to add to my vocabulary.

    // Methods for voice search:
    public void searchVoiceButton(View view) {
        promptSpeechInput();
    } // end searchVoiceButton() method.

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        // Make different locale for English and Romanian recogniser:
        String[] myLanguages = new String[2];
        myLanguages[0] = "en";
        myLanguages[1] = "ro";

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, myLanguages[direction]);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, myLanguages[direction]);
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, myLanguages[direction]);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, aSpeechDirection[direction]);
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_LONG).show();
        }
    } // end promptSpeechInput() method.

    // A method to change the separator between words and explanations:
    public void chooseSeparator() {
        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);
        AlertDialog separatorDialog;
        // Strings to Show In Dialog with Radio Buttons
        final CharSequence[] items = {getString(R.string.we_separator_dash), getString(R.string.we_separator_hiphen), getString(R.string.we_separator_pipe), getString(R.string.we_separator_newline)};
        final String[] weSeparators = {" – ", " - ", " | ", "<br>"};
        tempWESeparator = MainActivity.weSeparator;

        // Creating and Building the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.we_separator_title_dialog));
        // Determine current item selected:
        int weSeparatorPosition = -1;
        for (int i = 0; i < weSeparators.length; i++) {
            if (weSeparators[i].equals(MainActivity.weSeparator)) {
                weSeparatorPosition = i;
                break;
            }
        } // end for search current position of the current separator chosen.
        builder.setSingleChoiceItems(items, weSeparatorPosition, (dialog, item) -> {

            /*
             * Your code when an option selected,:
             */
            tempWESeparator = weSeparators[item];
        });

        builder.setPositiveButton(getString(R.string.we_separator_positive_dialog), (dialog, whichButton) -> {

            // Save the separator chosen:
            MainActivity.weSeparator = tempWESeparator;
            Settings set = new Settings(mFinalContext);
            set.saveStringSettings("weSeparator", MainActivity.weSeparator);
        }).setNegativeButton(getString(R.string.we_separator_negative_dialog), null);
        separatorDialog = builder.create();
        separatorDialog.show();
    } // end chooseSeparator() method.

    // A method to search a word from history:.
    public void searchFromHistory(String word, final int historyDirection) {
        // Find the edit text to put there the word from history:
        EditText et = findViewById(R.id.etWord);
        et.setText(word);

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Do something after a while:
            getWordFromDB(historyDirection);
        }, 300);
    } // end searchFromHistory.

    // A method to reset dictionary from menu and also from XML interface:
    public void resetToDefaults() {
        // Get the strings to make an alert:
        String tempTitle = getString(R.string.title_default_settings);
        String tempBody = getString(R.string.body_default_settings);
        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);

        new AlertDialog.Builder(context).setTitle(tempTitle).setMessage(tempBody).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(R.string.yes, (dialog, whichButton) -> {
            Settings set = new Settings(mFinalContext);
            set.setDefaultSettings();
            set.chargeSettings();
            // We must re-initialise also the TTS
            // settings:
            speak = new SpeakText(mFinalContext);
            speak2 = new SpeakText2(mFinalContext);
            // We need also to delete the search
            // history:
            searchHistory.deleteSearchHistory();

            /*
             * Get the strings to make an alert for reset
             * the vocabulary:
             */
            String tempTitle1 = getString(R.string.title_default_vocabulary);
            String tempBody1 = getString(R.string.body_default_vocabulary);
            Context context1 = new ContextThemeWrapper(mFinalContext, R.style.MyAlertDialog);
            new AlertDialog.Builder(context1).setTitle(tempTitle1).setMessage(tempBody1).setIcon(android.R.drawable.ic_delete).setPositiveButton(R.string.yes, (dialog1, whichButton1) -> {
                Settings set1 = new Settings(mFinalContext);
                set1.saveIntSettings("db2Ver", 0);
            }).setNegativeButton(R.string.no, null).show();
            /* End dialog for delete vocabulary at reset. */
        }).setNegativeButton(R.string.no, null).show();
    } // end resetToDefaults() method.

    // Here for datamuse:

    /*
     * This method creates LexicalResources object and calls the method with
     * same name as this from that class:
     */
    private void getExternalResource(int type, String word, String explanation) {
        LexicalResources lr = new LexicalResources(this);
        lr.getExternalResource(type, word, explanation);
    } // end getExternalResource() method.

    // This method sets the locale independent of the OS language:
    @TargetApi(24)
    public void setLocale(int numLang) {
        // If it is not the device language, normal change:
        if (numLang > 0) {
            // GUITools.beep();
            // An array with language codes:
            String[] langs = {"", "en", "ro"};
            String lang = langs[numLang];
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = getBaseContext().getResources().getConfiguration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        } // end if langNumber is not 0, no device language.
    } // end setLocale() method.

    // The method called from Settings menu:
    private void chooseLanguage() {
        AlertDialog languageDialog;
        // Creating and Building the Dialog:
        final Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Strings to Show In Dialog with Radio Buttons
        final CharSequence[] items = {getString(R.string.lang_device_language), getString(R.string.lang_english), getString(R.string.lang_romanian)};
        final int[] langNumbers = {0, 1, 2};

        builder.setTitle(getString(R.string.choose_language_dialog));
        // Determine current item selected:
        int langNumberPosition = -1;
        for (int i = 0; i < langNumbers.length; i++) {
            if (langNumbers[i] == langNumber) {
                langNumberPosition = i;
                break;
            }
        } // end for search current position of the current language chosen
        // before.
        builder.setSingleChoiceItems(items, langNumberPosition, (dialog, item) -> {

            // A temporary langNumber:
            MainActivity.tempLang = MainActivity.langNumber;
            switch (item) {
                case 0:
                    /*
                     * Your code when first option selected, device
                     * language:
                     */
                    MainActivity.tempLang = langNumbers[item];
                    break;

                case 1:
                    // Your code when 2nd option selected, English:
                    MainActivity.tempLang = langNumbers[item];
                    break;

                case 2:
                    // Your code when 3rd option selected, Romanian:
                    MainActivity.tempLang = langNumbers[item];
                    break;
            } // end switch.
        });

        builder.setPositiveButton(getString(R.string.msg_ok), (dialog, whichButton) -> {
            // Only if it was a choice:
            if (MainActivity.langNumber != MainActivity.tempLang) {
                MainActivity.langNumber = MainActivity.tempLang;
                Settings set = new Settings(context);
                set.saveIntSettings("langNumber", MainActivity.langNumber);
                recreateThisActivity();
            } // end if it is a different choice than before.
            //
        });
        languageDialog = builder.create();
        languageDialog.show();
    } // end chooseLanguage() method.

    // A method to update dictionary:
    private void updateNowNewWords() {
        UpdateDictionary ud = new UpdateDictionary(this);
        ud.updateStart();

    } // end updateNow() method.

    // A method to propose new words:
    private void proposeNowNewWords() {
        UpdateDictionary ud = new UpdateDictionary(this);
        ud.proposeStart();
    } // end proposeNowNewWords() method.

    /*
     * Some small methods with VIEW at parameter for the simulated action bar in
     * TV layout. These are necessary because we need to click them from layouts
     * resource.
     */

    // This is when pressing on status text with number of words:
    public void goShowLastDBUpdate(View view) {
        GUITools.showLastDBUpdate(this);
    } // end goShowLastDBUpdate() method.

    public void goToVocabulary(View view) {
        GUITools.goToVocabulary(this);
    } // end goToVocabularyMethod.

    public void goToIrregularVerbs(View view) {
        GUITools.goToIrregularVerbs(this);
    } // end goToIrregularVerbs() method.

    public void goToHistory(View view) {
        GUITools.goToHistory(this);
    } // end goToHistoryMethod.

    public void goToSettings(View view) {
        goToSettings();
    }

    public void goToTTSSettings(View view) {
        goToTTSSettings();
    }

    public void chooseSeparator(View view) {
        chooseSeparator();
    }

    public void chooseTVLanguage(View view) {
        chooseLanguage();
    }

    public void goToUpdateNowNewWords(View view) {
        updateNowNewWords();
    }

    public void goToProposeNowNewWords(View view) {
        proposeNowNewWords();
    }

    public void goToDisplaySettings(View view) {
        goToDisplaySettings();
    }

    public void goToBackgroundSettings(View view) {
        goToBackgroundSettings();
    }

    public void openAppInPlayStore(View view) {
        GUITools.openAppInPlayStore(this);
    }

    public void goToUpgrade(View view) {
        upgradeAlert();
    }

    public void goToHelp(View view) {
        GUITools.showHelp(this);
    }

    public void goToAbout(View view) {
        GUITools.aboutDialog(this);
    }

    public void goToRate(View view) {
        GUITools.showRateDialog(this);
    }

    public void resetToDefaults(View view) {
        resetToDefaults();
    }

    /*
     * End methods to go in different parts from simulated action bar in TV //
     * layout.
     */


    // Now for billing:
    // In app billing section starts here:
    public void upgradeToPremium(View view) {
        upgradeAlert();
    } // end upgradeToPremium() method.

    public void upgradeAlert() {
        // Make a context for this alert dialog:
        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);
        // Create now the alert:
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        if (GUITools.isNetworkAvailable(this)) {
            ScrollView sv = new ScrollView(context);
            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);
            // The message:
            TextView tv = new TextView(context);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
            String message;
            if (isPremium) {
                message = getString(R.string.premium_version_alert_message);
            } else {
                message = String.format(getString(R.string.non_premium_version_alert_message), mUpgradePrice);
            } // end if is not premium.
            tv.setText(message);
            tv.setFocusable(true);
            ll.addView(tv);
            // Add the LinearLayout into ScrollView:
            sv.addView(ll);
            alertDialog.setTitle(getString(R.string.premium_version_alert_title));
            alertDialog.setView(sv);
            // The button can be close or Get now!:
            String buttonName;
            if (isPremium) {
                buttonName = getString(R.string.bt_close);
            } else {
                buttonName = getString(R.string.bt_buy_premium);
            }
            alertDialog.setPositiveButton(buttonName, (dialog, whichButton) -> {
                // Start the payment process:
                // Only if is not premium:
                if (!isPremium) {
                    upgradeToPremiumActions();
                }
            });
            alertDialog.create();
            alertDialog.show();
        } // end if is connection available.
        else {
            GUITools.alert(this, getString(R.string.warning), getString(R.string.no_connection_available));
        } // end if connection is not available.
    } // end upgradeAlert() method.

    public void upgradeToPremiumActions() {
        initiatePurchase();
    } // end upgradeToPremiumActions() method.

    private void startBillingDependencies() {
        purchasesUpdatedListener = (billingResult, purchases) -> {
            // If item newly purchased
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                } // end for.
            }
            // If item already purchased then check and reflect changes
            else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                recreateThisActivityAfterRegistering();
            }
            //if purchase cancelled
            else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                GUITools.alert(mFinalContext, getString(R.string.warning), getString(R.string.purchase_canceled));
            }
            // Handle any other error messages
            else {
                GUITools.alert(mFinalContext, getString(R.string.warning), getString(R.string.billing_unknown_error));
            }
        };

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases(com.android.billingclient.api.PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here,
                    QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(listOf(QueryProductDetailsParams.Product.newBuilder().setProductId(mProduct).setProductType(BillingClient.ProductType.INAPP).build())).build();

                    // Now check if it is already purchased:
                    billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), (billingResult12, purchases) -> {
                        // check billingResult and process returned purchase list, e.g. display the products user owns
                        if (purchases != null && purchases.size() > 0) { // it means there are items:
                            Purchase myOldPurchase = purchases.get(0);
                            if (myOldPurchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                recreateThisActivityAfterRegistering();
                            }
                        } // end process the purchases list.
                    });
                    // end check if it is already purchased.

                    // Now let's query for our product:
                    billingClient.queryProductDetailsAsync(queryProductDetailsParams, (billingResult1, queryProductDetailsResult) -> {
                        // check billingResult
                        // process returned productDetailsList from the result object
                        if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            myProducts = queryProductDetailsResult.getProductDetailsList();
                            // Get the price of the 0 item if there is at least one product:
                            if (myProducts != null && myProducts.size() > 0) {
                                ProductDetails productDetail = myProducts.get(0);
                                ProductDetails.OneTimePurchaseOfferDetails offer = productDetail.getOneTimePurchaseOfferDetails();
                                if (offer != null) {
                                    mUpgradePrice = offer.getFormattedPrice();
                                }
                            }
                        }
                    });
                    // End query purchase.
                }
            } // end startConnection successfully.

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        }); // end startConnection.
    } // end startBillingDependencies() method.

    private void initiatePurchase() {
        // We purchase here the only one item found in myProducts list:
        if (myProducts != null && myProducts.size() > 0) { // only if there is at least one product available:
            ProductDetails productDetails = myProducts.get(0);

// An activity reference from which the billing flow will be launched.
            Activity activity = this;

            List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = listOf(BillingFlowParams.ProductDetailsParams.newBuilder()
                    // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                    .setProductDetails(productDetails).build());

            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build();

// Launch the billing flow
            BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);
        } // end if there is at least one productDetails object in myProducts list.
        else { // no items available:
            GUITools.alert(mFinalContext, getString(R.string.warning), getString(R.string.no_purchases_available));
        }
    } // end initiatePurchase() method.

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();

                // Updated for Billing Library 8.0.0 - using callback instead of deprecated listener
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        // if purchase is acknowledged
                        // Grant entitlement to the user. and restart activity
                        recreateThisActivityAfterRegistering();
                    }
                });
            }
        }
    } // end handlePurchase() method.

    // A method which recreates this activity after upgrading:
    private void recreateThisActivityAfterRegistering() {
        // We save it as an premium version:
        isPremium = true;
        Settings set = new Settings(this);
        set.saveBooleanSettings("isPremium", true);
        // This will go in a meteoric activity and will come back:
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            Intent intent = new Intent(MainActivity.this, PremiumVersionActivity.class);
            startActivity(intent);
            finish();
        });
    } // end recreateThisActivity() method.
    // End methods for InAppBilling.

} // end MainActivity class.
