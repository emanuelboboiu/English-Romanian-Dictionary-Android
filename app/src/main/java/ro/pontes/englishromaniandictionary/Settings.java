package ro.pontes.englishromaniandictionary;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * Class started on Sunday, 31 May 2015, created by Emanuel Boboiu.
 * This class contains useful methods like save or get settings.
 * */

public class Settings {

    // The file name for save and load preferences:
    private final static String PREFS_NAME = "derSettings";

    private final Context context;

    // The constructor:
    public Settings(Context context) {
        this.context = context;
    } // end constructor.

    // A method to detect if a preference exist or not:
    public boolean preferenceExists(String key) {
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.contains(key);
    } // end detect if a preference exists or not.

    // Methods for save and read preferences with SharedPreferences:
    // Save a boolean value:
    public void saveBooleanSettings(String key, boolean value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        // Commit the edits!
        editor.apply();
    } // end save boolean.

    // Read boolean preference:
    public boolean getBooleanSettings(String key) {
        boolean value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getBoolean(key, false);

        return value;
    } // end get boolean preference from SharedPreference.

    // Save a integer value:
    public void saveIntSettings(String key, int value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        // Commit the edits!
        editor.apply();
    } // end save integer.

    // Read integer preference:
    public int getIntSettings(String key) {
        int value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getInt(key, 0);

        return value;
    } // end get integer preference from SharedPreference.

    // For float values in shared preferences:
    public void saveFloatSettings(String key, float value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        // Commit the edits!
        editor.apply();
    } // end save float.

    // Read float preference:
    public float getFloatSettings(String key) {
        float value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getFloat(key, 3.0F); // a default value like the value
        // for moderate magnitude.

        return value;
    } // end get float preference from SharedPreference.

    // For double values in shared preferences:
    public void saveDoubleSettings(String key, double value) {
        // We cast the double to float:
        float tempValue = (float) value;
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, tempValue);
        // Commit the edits!
        editor.apply();
    } // end save double.

    // Read double preference:
    public double getDoubleSettings(String key) {
        float tempValue;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        tempValue = settings.getFloat(key, 3.0F);

        return (double) tempValue;
    } // end return a double from a float preference.

    // Save a String value:
    public void saveStringSettings(String key, String value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        // Commit the edits!
        editor.apply();
    } // end save String.

    // Read String preference:
    public String getStringSettings(String key) {
        String value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getString(key, null);

        return value;
    } // end get String preference from SharedPreference.
    // End read and write settings in SharedPreferences.

    // Charge Settings function:
    public void chargeSettings() {

        // Determine if is first launch of the program:
        boolean isNotFirstRunning = getBooleanSettings("isFirstRunning");

        if (!isNotFirstRunning) {
            saveBooleanSettings("isFirstRunning", true);
            // Make default values in SharedPreferences:
            setDefaultSettings();
        }

        // Now charge settings:

        // Play or not the sounds and speech:
        MainActivity.isSpeech = getBooleanSettings("isSpeech");
        MainActivity.isSound = getBooleanSettings("isSound");
        MainActivity.isSearchFullText = getBooleanSettings("isSearchFullText");

        // For blocking the device in portrait orientation:
        if (preferenceExists("isOrientationBlocked")) {
            MainActivity.isOrientationBlocked = getBooleanSettings("isOrientationBlocked");
        } // end if isOrientationBlocked preference exists.

        // For search history:
        if (preferenceExists("isHistory")) {
            MainActivity.isHistory = getBooleanSettings("isHistory");
        } // end if isHistory preference exists.

        // For done button of the keyboard to send a try:
        MainActivity.isImeAction = getBooleanSettings("isImeAction");

        // For text size:
        MainActivity.textSize = getIntSettings("textSize");

        // For the background of the activities:
        MainActivity.background = getStringSettings("background");

        // For the current language number chosen:
        MainActivity.langNumber = getIntSettings("langNumber");

        // For the separator between words and explanations:
        if (preferenceExists("weSeparator")) {
            MainActivity.weSeparator = getStringSettings("weSeparator");
        } // end if weSeparator preference exists.

        // Is shake detector or not:
        MainActivity.isShake = getBooleanSettings("isShake");
        // The magnitude of the shake detector:
        // MainActivity.onshakeMagnitude =
        // getFloatSettings("onshakeMagnitude");;

        // Wake lock, keep screen awake:
        MainActivity.isWakeLock = getBooleanSettings("isWakeLock");

        // For search direction:
        MainActivity.direction = getIntSettings("direction");

        // For general average:
        VerbsActivity.average = getDoubleSettings("average");

        // For sumOfMarks:
        VerbsActivity.sumOfMarks = getDoubleSettings("sumOfMarks");

        // For numberOfTests:
        VerbsActivity.numberOfTests = getIntSettings("numberOfTests");

        // Charge the premium status:
        MainActivity.isPremium = getBooleanSettings("isPremium");

        /* About number of launches, useful for information, rate and others: */
        // Get current number of launches:
        MainActivity.numberOfLaunches = getIntSettings("numberOfLaunches");
        // Increase it by one:
        MainActivity.numberOfLaunches++;
        // Save the new number of launches:
        saveIntSettings("numberOfLaunches", MainActivity.numberOfLaunches);
    } // end charge settings.

    public void setDefaultSettings() {

        // // Activate speech, sounds for dice and number speaking:
        saveBooleanSettings("isSpeech", true);
        saveBooleanSettings("isSound", true);
        saveBooleanSettings("isSearchFullText", false);
        saveBooleanSettings("isImeAction", true);
        saveBooleanSettings("isHistory", true);

        // The separator will be the long dash:
        MainActivity.weSeparator = " – ";
        saveStringSettings("weSeparator", MainActivity.weSeparator);

        // For text size for lines:
        saveIntSettings("textSize", 20);

        // Activate shake detection:
        saveBooleanSettings("isShake", false);

        // Activate orientation not to be portrait if is not TV:
        saveBooleanSettings("isOrientationBlocked", false);

        // Set on shake magnitude to 3.0F: // now default value, medium.
        saveFloatSettings("onshakeMagnitude", 3.0F);

        // For keeping screen awake:
        saveBooleanSettings("isWakeLock", true);

        // Save DataBases version to 0:
        saveIntSettings("dbVer", 0);

        // For search direction:
        saveIntSettings("direction", 0);

        // Set the average to 0.0:
        VerbsActivity.average = 0.0;
        saveDoubleSettings("average", VerbsActivity.average);

        // Set sum of all marks to 0.0:
        VerbsActivity.sumOfMarks = 0.0;
        saveDoubleSettings("sumOfMarks", VerbsActivity.sumOfMarks);

        // Save the number of tests tried to 0:
        VerbsActivity.numberOfTests = 0;
        saveIntSettings("numberOfTests", VerbsActivity.numberOfTests);

        // Delete also the last marks string:
        saveStringSettings("lastXMarks", null);

        // Make null also the unknown verbs:
        saveStringSettings("unknownVerbs", null);

        // We make it again as being not a premium one, to remove after:
        saveBooleanSettings("isPremium", false);

        // To be considered not noticed about premium version:
        saveBooleanSettings("wasNoticedPremium", false);

        /*
         * Save current engine to empty string, this way we will have again
         * Google TTS:
         */
        saveStringSettings("curEngine", "");

        // Save language, country and variant:
        saveStringSettings("ttsLanguage", "");
        saveStringSettings("ttsCountry", "");
        saveStringSettings("ttsVariant", "");
        saveFloatSettings("ttsRate", 1.0F);
        saveFloatSettings("ttsPitch", 1.0F);

        // The background must be saved to null:
        MainActivity.background = null;
        saveStringSettings("background", MainActivity.background);

        // The language number must be set to 0, device language:
        MainActivity.langNumber = 0;
        saveIntSettings("langNumber", MainActivity.langNumber);

        saveBooleanSettings("isDerivedForms", true);
        saveBooleanSettings("isArchaicForms", true);
    } // end setDefaultSettings function.

} // end Settings Class.
