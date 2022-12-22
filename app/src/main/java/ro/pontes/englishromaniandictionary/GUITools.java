package ro.pontes.englishromaniandictionary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

/*
 * Started on 21 June 2014, at 19:10 by Manu.
 * This class has some useful things for the GUI, like alerts.
 */

public class GUITools {

    /*
     * We need a global alertToShow as alert to be able to dismiss it when
     * needed and other things:
     */
    public static AlertDialog alertToShow;

    // A method to go to dictionary activity:
    public static void goToDictionary(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    } // end go to dictionary activity.

    // A method to go to history activity:
    public static void goToHistory(Context context) {
        Intent intent = new Intent(context, SearchHistoryActivity.class);
        context.startActivity(intent);
    } // end go to SearchHistoryActivity.

    // A method to go to verbs activity:
    public static void goToIrregularVerbs(Context context) {
        Intent intent = new Intent(context, VerbsActivity.class);
        context.startActivity(intent);
    } // end go to irregular verbs activity.

    // A method to go to vocabulary activity:
    public static void goToVocabulary(Context context) {
        Intent intent = new Intent(context, VocabularyActivity.class);
        context.startActivity(intent);
    } // end go to vocabulary activity.

    // A method to show an alert with title and message, just an OK button:
    public static void alert(Context parentContext, String title, String message) {

        Context context = new ContextThemeWrapper(parentContext,
                R.style.MyAlertDialog);

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        // The title:
        alert.setTitle(title);

        // The body creation:
        // Create a LinearLayout with ScrollView with all contents as TextViews:
        ScrollView sv = new ScrollView(context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        String[] mParagraphs = message.split("\n");

        // A for for each paragraph in the message as TextView:
        for (String mParagraph : mParagraphs) {
            TextView tv = new TextView(context);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
            tv.setFocusable(true);
            tv.setText(mParagraph);
            ll.addView(tv);
        } // end for.

        // Add now the LinearLayout into ScrollView:
        sv.addView(ll);

        alert.setView(sv);

        alert.setPositiveButton(context.getString(R.string.msg_ok),
                (dialog, whichButton) -> {
                    // Do nothing yet...
                });
        alert.show();
    } // end alert static method.

    // A method to show an alert with title and message in HTML:
    public static void alertHTML(Context parentContext, String title,
                                 String message, String btClose) {

        Context context = new ContextThemeWrapper(parentContext,
                R.style.MyAlertDialog);

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        // The title:
        alert.setTitle(title);

        // The body creation:
        // Create a LinearLayout with ScrollView with the content in an HTML
        // TextView:
        ScrollView sv = new ScrollView(context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(context);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
        tv.setFocusable(true);
        tv.setText(MyHtml.fromHtml(message));

        // Add the TextView into the LinearLayout:
        ll.addView(tv);

        // Add now the LinearLayout into ScrollView:
        sv.addView(ll);

        alert.setView(sv);

        alert.setPositiveButton(btClose, (dialog, whichButton) -> {
            // Do nothing yet...
        });
        alert.show();
    } // end alertHTML() static method.

    // A method for about dialog for this package:
    @SuppressLint("InflateParams")
    public static void aboutDialog(Context parentContext) {

        Context context = new ContextThemeWrapper(parentContext,
                R.style.MyAlertDialog);

        // Inflate the about message contents
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View messageView = inflater.inflate(R.layout.about_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // builder.setIcon(R.drawable.app_icon);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.setPositiveButton(context.getString(R.string.msg_close), null);
        builder.create();
        builder.show();
    } // end about dialog.

    // A method to play a tone, just to make tests:
    public static void beep() {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
    }

    // A method to give a toast, simple message on the screen:
    public static void toast(String message, int duration, Context context) {
        Toast.makeText(context, message, duration).show();
    } // end make toast.

    // A method to open the browser with an URL:
    private static final String HTTPS = "https://";
    private static final String HTTP = "http://";

    public static void openBrowser(final Context context, String url) {

        if (!url.startsWith(HTTP) && !url.startsWith(HTTPS)) {
            url = HTTP + url;
        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    } // end start browser with an URL in it.

    // A method to open the help online:
    public static void openHelp(final Context context) {
        openBrowser(context, "http://www.dictionar.limbalatina.ro/");
    } // end open help online method.

    // For formatting a date:
    public static String timeStampToString(Context context, int paramCurTime) {
        long curTime = (long) paramCurTime * 1000;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(curTime);

        // Now format the string:
        // See if it is today or yesterday:
        int today = getIsToday(curTime);
        String dayOfWeek;
        if (today == 1) {
            dayOfWeek = context.getString(R.string.today);
        } else if (today == 2) {
            dayOfWeek = context.getString(R.string.yesterday);
        } else {
            dayOfWeek = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
                    Locale.getDefault());
        }

        // Make the hour and minute with 0 in front if they are less
        // than 10:
        String curHour;
        int iHour = cal.get(Calendar.HOUR_OF_DAY);
        if (iHour < 10) {
            curHour = "0" + iHour;
        } else {
            curHour = "" + iHour;
        }
        String curMinute;
        int iMinute = cal.get(Calendar.MINUTE);
        if (iMinute < 10) {
            curMinute = "0" + iMinute;
        } else {
            curMinute = "" + iMinute;
        }

        return String.format(
                context.getString(R.string.date_format),
                dayOfWeek,
                "" + cal.get(Calendar.DAY_OF_MONTH),
                ""
                        + cal.getDisplayName(Calendar.MONTH, Calendar.LONG,
                        Locale.getDefault()),
                "" + cal.get(Calendar.YEAR), curHour, curMinute);
    } // end timeStampToString() method.

    /*
     * This method returns 1 if a date in milliseconds at parameter is today, 2
     * if it was yesterday or 0 on another date.
     */
    public static int getIsToday(long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis);

        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return 1; // today.
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return 2; // yesterday.
        } else if (smsTime.get(Calendar.DATE) - now.get(Calendar.DATE) == 1) {
            return 3; // tomorrow.
        } else {
            return 0; // another date.
        }
    } // end determine if a date is today or yesterday.

    // A static method to get a random number between two integers:
    public static int random(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    } // end random method.

    // A method to go to web page of the application:
    public static void goToAppWebPage(final Context context) {

        String url = "http://www.android.pontes.ro/erd/index.php?lang=";

        // Determine if is RO at the start of the current language:
        // Get the system current locale: // Get the locale:
        String curLocale = Locale.getDefault().getDisplayName();
        curLocale = curLocale.substring(0, 2);
        curLocale = curLocale.toLowerCase(Locale.getDefault());
        if (curLocale.equals("ro")) {
            url += "ro";
        } else {
            url += "en";
        }

        // Add also the google ID:
        url += "&google_id=" + MainActivity.myAccountName;

        // Call now the openBrowser method():
        openBrowser(context, url);
    } // end goToAppWebPage() method.

    // A method to open statistics site:
    public static void viewOnlineStatistics(final Context context) {

        String url = "http://www.android.pontes.ro/erd/stats";

        // Determine if is RO at the start of the current language:
        // Get the system current locale: // Get the locale:
        String curLocale = Locale.getDefault().getDisplayName();
        curLocale = curLocale.substring(0, 2);
        curLocale = curLocale.toLowerCase(Locale.getDefault());
        if (curLocale.equals("ro")) {
            url += "ro.php";
        } else {
            // Open the English version:
            url += "en.php";
        }

        // Add also the google ID:
        url += "?google_id=" + MainActivity.myAccountName;

        // Call now the openBrowser method():
        openBrowser(context, url);
    } // end viewOnlineStatistics() method.

    // A method for an alert to change nickname:
    public static void changeNickname(Context parentContext, String oldNickname) {
        final Context context = new ContextThemeWrapper(parentContext,
                R.style.MyAlertDialog);

        if (isNetworkAvailable(context)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);

            // The title:
            alert.setTitle(context.getString(R.string.nickname_title));

            // See if there is a current nickname on the server:
            if (oldNickname == null || oldNickname.length() < 1) {
                oldNickname = context.getString(R.string.unknown);
            }

            // The body:

            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);

            // A LayoutParams to add next items into addLLMain:
            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            // The text view where we say about nickname:
            TextView tv = new TextView(context);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
            String tempMessage = String.format(
                    context.getString(R.string.change_nickname_message),
                    oldNickname);

            tv.setText(tempMessage);
            tv.setFocusable(true);
            ll.addView(tv, llParams);

            // Set an EditText view to get user input
            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            input.setHint(context.getString(R.string.change_nickname_hint));
            // Add also an action listener:
            input.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Next two line are also at the done button pressing:
                    String newNickname = input.getText().toString();
                    changeNicknameFinishing(context, newNickname);
                    alertToShow.dismiss();
                }
                return false;
            });
            // End add action listener for the IME done button of the keyboard..

            ll.addView(input, llParams);

            alert.setView(ll);

            // end if OK was pressed.
            alert.setPositiveButton(context.getString(R.string.msg_ok),
                    (dialog, whichButton) -> {
                        // Next two line are also at the done button
                        // pressing:
                        String newNickname = input.getText().toString();
                        changeNicknameFinishing(context, newNickname);
                    });

            alert.setNegativeButton("Cancel",
                    (dialog, whichButton) -> {
                        // cancelled.
                    });

            alertToShow = alert.create();
            alertToShow.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            alertToShow.show();
            // end of alert dialog with edit sequence.
        } // end if is connection available.
        else {
            GUITools.alert(context, context.getString(R.string.warning),
                    context.getString(R.string.no_connection_available));
        }
    } // end changeNickname() method.

    // A method to check and finish the nickname changing:
    public static void changeNicknameFinishing(final Context context,
                                               String newNickname) {
        // Check if the nickname is longer than one character:
        if (newNickname.length() < 2) {
            GUITools.alert(context, context.getString(R.string.error),
                    context.getString(R.string.nickname_too_short));
        } else {
            // A good nickname was written:
            // Here save it into server database:
            Statistics stats = new Statistics(context);
            stats.postNewName(MainActivity.myAccountName, newNickname);
            GUITools.alert(context, context.getString(R.string.nickname_title),
                    String.format(context.getString(R.string.nickname_success),
                            newNickname));
        }
    } // end check and change the nickname method.

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    } // end isNetworkAvailable() method.

    public static void copyIntoClipboard(final Context context, String text) {
        SoundPlayer.playSimple(context, "copy_into_clipboard");
        ClipboardManager clipboard = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Result", text);
        clipboard.setPrimaryClip(clip);
    } // end copyIntoClipboard() method.

    // A method to show help in an alert LinearLayout:
    public static void showHelp(Context parentContext) {

        final Context context = new ContextThemeWrapper(parentContext,
                R.style.MyAlertDialog);

        // Create a LinearLayout with ScrollView with all contents as TextViews:
        ScrollView sv = new ScrollView(context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        // Get the items to be shown in alert:
        Resources res = context.getResources();
        String[] aInformation = res.getStringArray(R.array.information_array);

        // A for for each message in the history as TextView:
        for (String s : aInformation) {
            TextView tv = new TextView(context);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
            tv.setPadding(MainActivity.mPaddingDP, MainActivity.mPaddingDP,
                    MainActivity.mPaddingDP, MainActivity.mPaddingDP);
            tv.setText(MyHtml.fromHtml(s));
            tv.setFocusable(true);
            ll.addView(tv);
        } // end for.
        sv.addView(ll);

        // Create now the alert:
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(context.getString(R.string.help_alert_title));
        alertDialog.setView(sv);
        alertDialog.setPositiveButton(context.getString(R.string.bt_close),
                null);
        AlertDialog alert = alertDialog.create();
        alert.show();
    } // end showHelp() method.

    /*
     * A method which makes an unique string. This is for premium outside Google
     * mechanisms:
     */
    public static String getUniqueIdFromAccountName(final String str) {
        String toReturn = "xyzxyzxyz890890";
        try {
            toReturn = StringTools.SHA1(str);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return toReturn.substring(1, 11).toLowerCase(Locale.US);
    } // end get the SHA1 string for registration.

    // A method to round a double value:
    public static double round(double number, int decimals) {
        double temp = Math.pow(10, decimals);
        return Math.round(number * temp) / temp;
    } // end round() method.

    // A method to get current time in seconds:
    public static long getTimeInSeconds() {
        Calendar cal = Calendar.getInstance();
        long timeInMilliseconds = cal.getTimeInMillis();
        return timeInMilliseconds / 1000;
    } // end getTimeInSeconds() method.

    // A method to rate this application:
    public static void showRateDialog(Context parentContext) {
        final Context context = new ContextThemeWrapper(parentContext,
                R.style.MyAlertDialog);

        AlertDialog.Builder builder = new AlertDialog.Builder(context).setIcon(
                R.drawable.ic_launcher).setTitle(
                context.getString(R.string.title_rate_app));

        ScrollView sv = new ScrollView(context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(context);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
        tv.setFocusable(true);
        tv.setText(context.getString(R.string.body_rate_app));
        ll.addView(tv);

        // Add now the LinearLayout into ScrollView:
        sv.addView(ll);

        builder.setView(sv);
        builder.setPositiveButton(context.getString(R.string.bt_rate),
                (dialog, which) -> {
                    Settings set = new Settings(context);
                    set.saveBooleanSettings("wasRated", true);
                    String link = "market://details?id=";
                    try {
                        // play market available
                        context.getPackageManager().getPackageInfo(
                                "com.android.vending", 0);
                        // not available
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        // Should use browser
                        link = "https://play.google.com/store/apps/details?id=";
                    }
                    // Starts external action
                    context.startActivity(new Intent(
                            Intent.ACTION_VIEW, Uri.parse(link
                            + context.getPackageName())));
                }).setNegativeButton(context.getString(R.string.bt_not_now),
                null);
        builder.show();
    } // end showRateDialog() method.

    // A method which checks if was rated:
    public static void checkIfRated(Context context) {
        Settings set = new Settings(context);
        boolean wasRated = set.getBooleanSettings("wasRated");
        if (!wasRated) {

            if (MainActivity.numberOfLaunches % 6 == 0
                    && MainActivity.numberOfLaunches > 0) {
                GUITools.showRateDialog(context);
            } // end if was x launches.
        } // end if it was not rated.
    } // end checkIfRated() method.

    // A method which checks if user was noticed about premium version:
    public static void checkIfNoticedAboutPremium(final Context context) {
        // only if is not already the premium version:
        if (!MainActivity.isPremium) {
            final Settings set = new Settings(context);
            boolean wasNoticedPremium = set
                    .getBooleanSettings("wasNoticedPremium");
            if (!wasNoticedPremium) {
                if (MainActivity.numberOfLaunches % 15 == 0
                        && MainActivity.numberOfLaunches > 0) {

                    final Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        // Do something after x milliseconds:

                        GUITools.alert(
                                context,
                                context.getString(R.string.information),
                                String.format(
                                        context.getString(R.string.info_about_premium_version),
                                        MainActivity.mUpgradePrice));
                        set.saveBooleanSettings("wasNoticedPremium", true);

                    }, 1500);

                } // end if was the x launch.
            } // end if it was not noticed.
        } // end if is not premium version.
    } // end checkIfNoticedAboutPremium() method.

    // A method to open the app in Play Store:
    public static void openAppInPlayStore(Context context) {
        final String appPackageName = context.getPackageName();
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("https://play.google.com/store/apps/details?id="
                            + appPackageName)));
        }
    } // end openAppInPlayStore() method.

    // A method to set background and other global things about a layout:
    public static void setLayoutInitial(Context context, int layoutType) {
        String mBackground;
        if (MainActivity.background == null
                || MainActivity.background.equals("")) {
            /*
             * It means no background was chosen and saved, we choose a random
             * one. If is Android TV we must set the 4 background, this was
             * considered the best for TV. It will no be a random one.
             */
            /*
             * Number of backgrounds, in the drawable folder. This number is
             * also specified in BacgroundsActivity:
             */

            int curBackgroundNumber = 4; // This will be the default, for TV.
            if (!MainActivity.isTV) {
                int nrOfBackgrounds = 5;
                curBackgroundNumber = GUITools.random(1, nrOfBackgrounds);
            } // end is not a TV and we generate a random background.
            mBackground = "paper" + curBackgroundNumber;
            // We save the one chosen by random:
            MainActivity.background = mBackground;
            Settings set = new Settings(context);
            set.saveStringSettings("background", MainActivity.background);
        } else {
            /* It means is was saved, we get it from the static String variable: */
            mBackground = MainActivity.background;
        } // end if a background was chosen.

        // Determine the background ID:
        int resId = 0;
        if (!MainActivity.background.equals("paper0")) {
            resId = context.getResources().getIdentifier(mBackground,
                    "drawable", context.getPackageName());
        }

        /* layoutType 1 means relative, 2 means linear. */
        // if is a relative layout:
        if (layoutType == 1) {
            RelativeLayout rl = (RelativeLayout) ((Activity) context)
                    .findViewById(R.id.layoutMain);
            rl.setBackgroundResource(resId);
        } // end if layoutType is RelativeLayout.
        // Now for LinearLayout:
        else if (layoutType == 2) {
            LinearLayout ll = (LinearLayout) ((Activity) context)
                    .findViewById(R.id.layoutMain);
            ll.setBackgroundResource(resId);
        } // end if is a LinearLayout.
    } // end setLayoutInitial() method.

    public static boolean isAndroidTV(final Context context) {
        UiModeManager uiModeManager = (UiModeManager) context
                .getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            return true;
        } else {
            return false;
        }
    } // end isAndroidTV() method.

    public static void showLastDBUpdate(Context context) {
        Settings set = new Settings(context);
        int lastUpdate = set.getIntSettings("lastUpdate");
        String tempLast = GUITools.timeStampToString(context, lastUpdate);
        String message = String.format(
                context.getString(R.string.last_db_update), tempLast);
        GUITools.alertHTML(context, context.getString(R.string.information),
                message, context.getString(R.string.msg_ok));
    } // end showLastDBUpdate|() method.

} // end GUITools class.
