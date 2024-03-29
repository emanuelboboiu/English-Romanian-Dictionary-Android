package ro.pontes.englishromaniandictionary;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TTSSettingsActivity extends Activity {

    private final Context mFinalContext = this;

    // The main object for TextToSpeech:
    private TextToSpeech tts;
    private String chosenEngine = "";
    private float ttsRate = 1.0F;
    private float ttsPitch = 1.0F;
    private final float granularity = 10F;
    private final float granularityMultiplier = 10F;
    float granularity2 = granularity * granularityMultiplier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts_settings);
        // Set the orientation to be portrait if needed:
        if (MainActivity.isOrientationBlocked && !MainActivity.isTV) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } // end if isOrientationBlocked is true.

        // Some initial things like background:
        GUITools.setLayoutInitial(this, 2);

        detectEngines();

    } // end onCreate() method.

    // The onDestroy() method:
    @Override
    protected void onDestroy() {
        tts.shutdown();
        super.onDestroy();
    } // end onDestroy() method.

    // A method which detects all engines and their voices:
    private void detectEngines() {

        // TextToSpeech initialisation:
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                // tts.setLanguage(locale.US);
            }
        });

        List<TextToSpeech.EngineInfo> mEngines = tts.getEngines();

        // First of all we take the llEngines LinearLayout:
        LinearLayout llEngines = (LinearLayout) findViewById(R.id.llEngines);
        llEngines.removeAllViews();

        /*
         * A LayoutParams for weight for text view and buttons in the llEngines,
         * to be 1F::
         */
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        llParams.weight = 1.0f;

        /*
         * Make the text view for information about how many engines are
         * available:
         */
        TextView tv = new TextView(this);

        int count = mEngines.size();
        Resources res = getResources();
        String foundEngines = res.getQuantityString(R.plurals.tv_number_of_engines, count, count);
        tv.setText(foundEngines);
        tv.setFocusable(true);
        llEngines.addView(tv, llParams);

        // Make buttons for each engine found:
        for (int i = 0; i < mEngines.size(); i++) {
            Button bt = new Button(this);
            final String engineLabel = mEngines.get(i).label;
            bt.setText(engineLabel);
            final String engineName = mEngines.get(i).name;

            bt.setOnClickListener(view -> {
                // Let's initialise the chosen TTS engine:
                // TextToSpeech initialisation:
                tts = new TextToSpeech(mFinalContext, status -> {
                    if (status != TextToSpeech.ERROR) {
                        // We do something good:
                        detectVoices(engineName, engineLabel);
                    } else {
                        // Show an error:
                        GUITools.alert(mFinalContext, getString(R.string.error), getString(R.string.error_after_choosing_engine));
                    }
                }, engineName);
                // End initialisation.
            });
            // End add listener for tap on button.
            llEngines.addView(bt, llParams);
        } // end for.
    } // end detectEngines() method.

    // A method to show voices of an engine:
    private void detectVoices(String engineName, final String engineLabel) {
        SoundPlayer.playSimple(this, "element_clicked");
        chosenEngine = engineName; // to have it for saving.
        // First of all we take the llVoices LinearLayout:
        LinearLayout llVoices = (LinearLayout) findViewById(R.id.llVoices);
        llVoices.removeAllViews();

        /*
         * A LayoutParams for weight for text view and buttons in the llVoices,
         * to be 1F::
         */
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        llParams.weight = 1.0f;

        // A text view as a title of this window zone:
        TextView tv = new TextView(this);
        tv.setText(MyHtml.fromHtml(String.format(getString(R.string.tv_chose_a_voice), engineLabel)));
        tv.setFocusable(true);
        llVoices.addView(tv, llParams);

        // Detect all voice locale:
        ArrayList<Locale> localeList = new ArrayList<>();

        try {
            // If is a newer version of Android, API 21:
            Set<Locale> tempLocaleList = tts.getAvailableLanguages();
            localeList = new ArrayList<>(tempLocaleList);
        } catch (Exception e) {
            // e.printStackTrace();
        } // end try .. catch block.

        // We have all languages of this TTS:

        // Now we extract only the English variants removing the others:
        if (localeList != null && localeList.size() > 0) {
            for (int i = localeList.size() - 1; i >= 0; i--) {
                String curTempLang = localeList.get(i).getLanguage();
                if (!(curTempLang.equalsIgnoreCase("en") || curTempLang.equalsIgnoreCase("eng"))) {
                    localeList.remove(i);
                } // end if current Locale isn't English.
            } // end for cut the non-English locale.
        } // end if there is at least one locale.

        // Now, let's create buttons for each voice found:
        // If no languages are available, we must show a text view:
        if (localeList == null || localeList.size() < 1) {
            TextView tv2 = new TextView(this);
            tv2.setText(getString(R.string.tv_no_english_available));
            tv2.setFocusable(true);
            llVoices.addView(tv2, llParams);
        } // end if no languages were detected.

        // If languages were detected:
        else {
            for (int i = 0; i < localeList.size(); i++) {
                Button bt = new Button(this);
                final Locale tempLocale = localeList.get(i);
                String language = tempLocale.getDisplayLanguage();
                String country = tempLocale.getDisplayCountry();
                String variant = tempLocale.getDisplayVariant();
                CharSequence curLanguage = MyHtml.fromHtml(String.format(getString(R.string.one_english_voice_in_list), language, country, variant));
                bt.setText(curLanguage.toString().trim());
                bt.setOnClickListener(view -> showVoice(tempLocale, engineLabel));
                // End add listener for tap on button.
                llVoices.addView(bt, llParams);
            } // end for each button for a language.
        } // end if at least one language was detected.
    } // end detectVoices();

    // A method to detail the chosen voice:
    private void showVoice(final Locale tempLocale, String engineLabel) {
        SoundPlayer.playSimple(this, "element_clicked");
        // First of all we take the llVoices LinearLayout:
        LinearLayout llVoices = (LinearLayout) findViewById(R.id.llVoices);
        llVoices.removeAllViews();

        // A LayoutParams to add some controls into llVoices:
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // Write a title for this zone:
        TextView tv = new TextView(this);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        String language = tempLocale.getDisplayLanguage();
        String country = tempLocale.getDisplayCountry();
        String variant = tempLocale.getDisplayVariant();
        CharSequence curLanguage = MyHtml.fromHtml(String.format(getString(R.string.one_english_voice_in_list), language, country, variant));

        CharSequence voiceTitle = MyHtml.fromHtml(String.format(getString(R.string.a_voice_chosen_to_check), engineLabel, curLanguage.toString().trim()));
        tv.setText(voiceTitle);
        tv.setFocusable(true);
        llVoices.addView(tv, llParams);

        // We set the chosen voice:
        tts.setLanguage(tempLocale);

        // See about rate and pitch if there are saved:
        Settings set = new Settings(this);
        if (set.preferenceExists("ttsRate")) {
            ttsRate = set.getFloatSettings("ttsRate");
        } // end if TTS rate was saved.
        tts.setSpeechRate(ttsRate);

        if (set.preferenceExists("ttsPitch")) {
            ttsPitch = set.getFloatSettings("ttsPitch");
        } // end if TTS rate was saved.
        tts.setPitch(ttsPitch);

        // We need here two seek controls:
        // For rate:
        TextView tvRate = new TextView(this);
        tvRate.setGravity(Gravity.CENTER_HORIZONTAL);
        tvRate.setText(getString(R.string.tv_set_tts_rate));
        tvRate.setFocusable(true);
        llVoices.addView(tvRate, llParams);

        SeekBar seekBarRate = new SeekBar(this);
        int intRate = getProcentFromFloat(ttsRate);
        seekBarRate.setProgress(intRate);
        seekBarRate.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ttsRate = getFloatFromProcent(progress);
                tts.setSpeechRate(ttsRate);
                speakTheSample("" + progress + "%");
            }
        });
        // end listener for this SeekBar.
        llVoices.addView(seekBarRate, llParams);

        // For pitch:
        TextView tvPitch = new TextView(this);
        tvPitch.setGravity(Gravity.CENTER_HORIZONTAL);
        tvPitch.setText(getString(R.string.tv_set_tts_pitch));
        tvPitch.setFocusable(true);
        llVoices.addView(tvPitch, llParams);

        SeekBar seekBarPitch = new SeekBar(this);
        int intPitch = getProcentFromFloat(ttsPitch);
        seekBarPitch.setProgress(intPitch);
        seekBarPitch.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ttsPitch = getFloatFromProcent(progress);
                tts.setPitch(ttsPitch);
                speakTheSample("" + progress + "%");
            }
        });
        // end listener for this SeekBar.

        llVoices.addView(seekBarPitch, llParams);

        // We need an horizontal LinearLayout to hear a text as sample and save
        // button:
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setGravity(Gravity.CENTER_HORIZONTAL);

        // A LayoutParams for weight of buttons here as 1F:
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.weight = 1.0f;

        // A button to hear a sample:
        Button btSample = new Button(this);
        btSample.setText(getString(R.string.bt_tts_sample));
        final String sampleText = "Hi! I can read English language. If you like my voice, choose me clicking the Save button. Thanks!";
        btSample.setOnClickListener(view -> speakTheSample(sampleText));
        // End add listener for tap on btSample.
        btSample.setOnLongClickListener(view -> {
            CharSequence theMessage = MyHtml.fromHtml(String.format(getString(R.string.the_sample_message), getString(R.string.bt_tts_sample), sampleText));
            GUITools.alert(mFinalContext, getString(R.string.the_sample), theMessage.toString());
            return true;
        });
        // End add listener for tap on btSample.

        ll.addView(btSample, params);

        // A button to save this voice:
        Button btSave = new Button(this);
        btSave.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        btSave.setText(getString(R.string.bt_tts_save));
        btSave.setOnClickListener(view -> saveVoice(tempLocale));
        // End add listener for tap on btSave.
        ll.addView(btSave, params);
        llVoices.addView(ll, llParams);
    }// end showVoice() method.

    // A method to speak a sample:
    private void speakTheSample(final String text) {
        // Speak the sample:
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Do something after 250ms:
            // Write the text to be spoken:
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }, 250);
    } // end speakTheSample() method.

    // A method to save the chosen TTS and voice:
    private void saveVoice(Locale tempLocale) {
        tts.stop();
        SoundPlayer.playSimple(this, "element_finished");
        String language = tempLocale.getLanguage();
        String country = tempLocale.getCountry();
        String variant = tempLocale.getVariant();

        // We save now the values:
        Settings set = new Settings(this);
        set.saveStringSettings("curEngine", chosenEngine);
        set.saveStringSettings("ttsLanguage", language);
        set.saveStringSettings("ttsCountry", country);
        set.saveStringSettings("ttsVariant", variant);
        set.saveFloatSettings("ttsRate", ttsRate);
        set.saveFloatSettings("ttsPitch", ttsPitch);

        // Go now into main windows, dictionary itself:
        GUITools.goToDictionary(this);
        this.finish();
    } // end saveVoice() method.

    /*
     * A method which transform the float in int value, 1.0 being 50%, 0.0 being
     * 0% and 10.0 being 100%.
     */
    private int getProcentFromFloat(float val) {
        int toReturn;

        if (val >= 1.0F) {
            toReturn = Math.round(50 + granularity * (val - 1));
        } else {
            toReturn = Math.round(50 - granularity2 * (1 - val));
        }

        return toReturn;
    } // end getProcentFromFloat() method.

    /* A method which makes the opposite thing, see the method above: */
    private float getFloatFromProcent(int proc) {
        float toReturn;

        if (proc >= 50) {
            toReturn = (proc - 40) / granularity;
        } else {
            toReturn = (proc + 50) / granularity2;
        }

        return toReturn;
    } // end getFloatFromProcent() method.

} // end TTSSettingsActivity class.

