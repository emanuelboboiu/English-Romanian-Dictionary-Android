package ro.pontes.englishromaniandictionary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CheckBox;

public class SettingsActivity extends Activity {

    final Context mFinalContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Set the orientation to be portrait if needed:
        if (MainActivity.isOrientationBlocked && !MainActivity.isTV) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } // end if isOrientationBlocked is true.

        // Some initial things like background:
        GUITools.setLayoutInitial(this, 2);

        // Check or check the check boxes, depending of current boolean values:

        // For sounds in program:
        CheckBox cbtSoundsSetting = (CheckBox) findViewById(R.id.cbtSoundsSetting);
        cbtSoundsSetting.setChecked(MainActivity.isSound);

        // For search full text in program:
        CheckBox cbtSearchFullTextSetting = (CheckBox) findViewById(R.id.cbtSearchFullTextSetting);
        cbtSearchFullTextSetting.setChecked(MainActivity.isSearchFullText);

        // For portrait orientation:
        CheckBox cbtPortraitOrientationSetting = (CheckBox) findViewById(R.id.cbtPortraitOrientationSetting);
        if (MainActivity.isTV) {
            // For Android TV we need it to be unavailable and unchecked:
            cbtPortraitOrientationSetting.setChecked(false);
            cbtPortraitOrientationSetting.setEnabled(false);
        } else {
            cbtPortraitOrientationSetting.setChecked(MainActivity.isOrientationBlocked);
        }

        // For shake:
        CheckBox cbtOnshakeSetting = (CheckBox) findViewById(R.id.cbtOnshakeSetting);
        if (MainActivity.isTV) {
            // For Android TV we need it to be unavailable and unchecked:
            cbtOnshakeSetting.setChecked(false);
            cbtOnshakeSetting.setEnabled(false);
        } else {
            cbtOnshakeSetting.setChecked(MainActivity.isShake);
        }

        // For keeping screen awake:
        CheckBox cbtScreenAwakeSetting = (CheckBox) findViewById(R.id.cbtScreenAwakeSetting);
        if (MainActivity.isTV) {
            // For Android TV we need it to be unavailable and unchecked:
            cbtScreenAwakeSetting.setChecked(false);
            cbtScreenAwakeSetting.setEnabled(false);
        } else {
            cbtScreenAwakeSetting.setChecked(MainActivity.isWakeLock);
        }

        // For IME DONE button of the keyboard:
        CheckBox cbtImeSetting = (CheckBox) findViewById(R.id.cbtImeSetting);
        cbtImeSetting.setChecked(MainActivity.isImeAction);

        // For search history, activated or not:
        CheckBox cbtHistorySetting = (CheckBox) findViewById(R.id.cbtHistorySetting);
        cbtHistorySetting.setChecked(MainActivity.isHistory);
    } // end onCreate() method.

    // Let's see what happens when a check box is clicked in audio settings:
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        Settings set = new Settings(this); // to save changes.

        // Check which check box was clicked
        if (view.getId() == R.id.cbtSoundsSetting) {
            MainActivity.isSound = checked;
            set.saveBooleanSettings("isSound", MainActivity.isSound);
        } else if (view.getId() == R.id.cbtSearchFullTextSetting) {
            MainActivity.isSearchFullText = checked;
            set.saveBooleanSettings("isSearchFullText", MainActivity.isSearchFullText);
        } else if (view.getId() == R.id.cbtPortraitOrientationSetting) {
            MainActivity.isOrientationBlocked = checked;
            set.saveBooleanSettings("isOrientationBlocked", MainActivity.isOrientationBlocked);
        } else if (view.getId() == R.id.cbtOnshakeSetting) {
            MainActivity.isShake = checked;
            set.saveBooleanSettings("isShake", MainActivity.isShake);
        } else if (view.getId() == R.id.cbtScreenAwakeSetting) {
            MainActivity.isWakeLock = checked;
            set.saveBooleanSettings("isWakeLock", MainActivity.isWakeLock);
        } else if (view.getId() == R.id.cbtImeSetting) {
            MainActivity.isImeAction = checked;
            set.saveBooleanSettings("isImeAction", MainActivity.isImeAction);
        } else if (view.getId() == R.id.cbtHistorySetting) {
            if (checked) {
                MainActivity.isHistory = true;
            } else {
                MainActivity.isHistory = false;
                // Try here to delete also the log:
                deleteLog();
            }
            set.saveBooleanSettings("isHistory", MainActivity.isHistory);
        }

        // Play also a sound:
        SoundPlayer.playSimple(this, "element_clicked");
    } // end onClick method.

    public void deleteLog() {

        String tempTitle = getString(R.string.sh_title_delete_history);
        String tempBody = getString(R.string.sh_disable_now);

        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);
        new AlertDialog.Builder(context).setTitle(tempTitle).setMessage(tempBody).setIcon(android.R.drawable.ic_delete).setPositiveButton(R.string.yes, (dialog, whichButton) -> {
            SearchHistory searchHistory = new SearchHistory(mFinalContext);
            searchHistory.deleteSearchHistory();
            // Play a delete sound:
            SoundPlayer.playSimple(mFinalContext, "delete_history");
        }).setNegativeButton(R.string.no, null).show();

    } // end deleteLog() method.

} // end settings activity class.
