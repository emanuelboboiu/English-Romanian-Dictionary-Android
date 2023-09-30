package ro.pontes.englishromaniandictionary;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class SpeakText2 {

    private final Context context;
    private final TextToSpeech mTTS;
    private Locale curTTSLocale = null;
    private float ttsRate = 1.0F;
    private float ttsPitch = 1.0F;

    // The constructor:
    public SpeakText2(Context context) {
        this.context = context;

        /*
         * First of all, charge current engine, the saved one, the default would
         * be Google TTS:
         */
        Settings set = new Settings(this.context);
        String curEngine = set.getStringSettings("curEngine2");
        // Check if a setting was saved:
        if (curEngine == null || curEngine.equals("")) {
            curEngine = "com.google.android.tts";
        } // end if no engine was saved.

        // For TextToSpeech:
        mTTS = new TextToSpeech(this.context, status -> {
            if (status != TextToSpeech.ERROR) {
                // It means no error was found, we can set the
                // language:
                setSavedLanguage();
            }
        }, curEngine);
        // end for TextToSpeech.
    } // end constructor.

    public void sayUsingLanguage(final String toSay, final boolean interrupt) {
        if (MainActivity.isSpeech) {

            // First check if there is this language:
            if (mTTS.isLanguageAvailable(curTTSLocale) != TextToSpeech.LANG_NOT_SUPPORTED) {

                int speakMode;
                if (interrupt) {
                    speakMode = TextToSpeech.QUEUE_FLUSH;
                } else {
                    speakMode = TextToSpeech.QUEUE_ADD;
                } // end if is not interruption.
                mTTS.speak(toSay, speakMode, null, null);

            } // end if this language exists as country and variant.
            else {
                GUITools.alert(context, context.getString(R.string.warning), context.getString(R.string.warning_no_tts_available_ro));
            } // end if the language doesn't exist.
        } // end if isSpeech.
    } // end sayUsingLanguage() method.

    // A method to spell a string:
    public void spellUsingLanguage(String toSpell) {
        if (MainActivity.isSpeech) {

            // First check if there is this language:
            if (mTTS.isLanguageAvailable(curTTSLocale) != TextToSpeech.LANG_NOT_SUPPORTED) {

                for (int i = 0; i < toSpell.length(); i++) {
                    mTTS.speak("" + toSpell.charAt(i), TextToSpeech.QUEUE_ADD, null, null);
                } // end for each character.

            } // end if this language exists as country and variant.
            else {
                GUITools.alert(context, context.getString(R.string.warning), context.getString(R.string.warning_no_tts_available_ro));
            } // end if the language doesn't exist.
        } // end if isSpeech.
    } // end spellUsingLanguage() method.

    public void stop() {
        mTTS.stop();
    } // end stop method of the SpeakText class.

    // A method to set the language of this instance, make a locale:
    private void setSavedLanguage() {
        Settings set = new Settings(context);
        String language = set.getStringSettings("ttsLanguage2");
        if (language == null || language.equals("")) {
            language = "ro";
        }
        String country = set.getStringSettings("ttsCountry2");
        if (country == null || country.equals("")) {
            country = Locale.getDefault().getCountry();
        }
        String variant = set.getStringSettings("ttsVariant2");
        if (variant == null) {
            variant = "";
        }

        curTTSLocale = new Locale(language, country, variant);

        mTTS.setLanguage(curTTSLocale);

        // See about rate and pitch:
        if (set.preferenceExists("ttsRate")) {
            ttsRate = set.getFloatSettings("ttsRate");
        } // end if TTS rate was saved.
        mTTS.setSpeechRate(ttsRate);

        if (set.preferenceExists("ttsPitch")) {
            ttsPitch = set.getFloatSettings("ttsPitch");
        } // end if TTS rate was saved.
        mTTS.setPitch(ttsPitch);
    } // end setCurrentLanguage() method.

    // A method to release resources:
    public void close() {
        mTTS.shutdown();
    } // end close() method.

} // end SpeakText class.
