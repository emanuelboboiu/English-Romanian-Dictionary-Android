package ro.pontes.englishromaniandictionary;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class QuizIrregularVerbs extends Quiz {

    private final ArrayList<Byte> arForms;
    private final Byte[] bObjects;
    private byte itemNumber;
    private final byte itemsLimit = 10; // the number of questions.
    private final TextView[] aTvs = new TextView[5];
    private final String[] aCurForms = new String[5];
    private final String[] aVerbForms;
    private int numberOfErrors;
    private final History mHistory;
    private final ArrayList<Short> arTempUnknownVerbs;
    private int lastVerbId = 0;
    private Button btNext; // we need it to be disabled or enabled.

    // The constructor:
    public QuizIrregularVerbs(Context context, LinearLayout llMainPart) {
        super(context, llMainPart);
        arForms = new ArrayList<>();
        bObjects = new Byte[]{0, 1, 2, 3};
        // Get now the array for check buttons, the forms names:
        Resources res = context.getResources();
        aVerbForms = res.getStringArray(R.array.verb_forms_array);
        itemNumber = 0;
        numberOfErrors = 0;
        mHistory = new History(this.context);

        // Initialise also the temp unknown verbs ArrayList:
        arTempUnknownVerbs = new ArrayList<>();
    } // end constructor.

    // The method to start this quiz:
    public void startQuiz() {
        clearLL();
        TextView tvTop = createTextView(context.getString(R.string.choose_forms_to_fill_in), textSize + 2);
        tvTop.setGravity(Gravity.CENTER_HORIZONTAL);
        tvTop.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        // A LayoutParams for weight for text view in the llTop, to be 1F::
        LinearLayout.LayoutParams tvTopParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        tvTopParams.weight = 1.0f;
        // Add the tvTop into llTop:
        llTop.addView(tvTop, tvTopParams);

        // We need here a LinearLayout horizontal for those check boxes:
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // A LayoutParams for weight of buttons here as 1F:
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.weight = 1.0f;

        // Now the for to create check boxes for each form:
        for (int i = 1; i < aVerbForms.length; i++) {
            CheckBox cbt = createCheckBox(aVerbForms[i], textSize);
            final byte form = (byte) i;
            cbt.setOnClickListener(view -> addOrRemoveChosenForms(form));
            // End add listener for tap on check box.
            ll.addView(cbt, params);
        } // end for create check boxes.

        // Add the start button:
        Button btStart = createButton(context.getString(R.string.bt_start_quiz), textSize);
        btStart.setOnClickListener(view -> startNow());
        // End add listener for tap on btStart.
        ll.addView(btStart, params);
        llTop.addView(ll, llParams);

        llMain.addView(createTextView(context.getString(R.string.quiz_message_before_start), textSize));
    } // end startQuiz() method.

    // A method to add or remove a verb form:
    private void addOrRemoveChosenForms(byte form) {
        /*
         * Check if form exists or not. If exists we remove it, otherwise we add
         * it:
         */
        if (arForms.contains(bObjects[form])) {
            arForms.remove(bObjects[form]);
        } else {
            arForms.add(bObjects[form]);
        }
        // Sort the arForms:
        Collections.sort(arForms);
    } // end addOrRemoveChosenForm() method.

    // The method to start effectively the quiz:
    private void startNow() {
        // Check first if at least a form was chosen:
        if (arForms.size() > 0) {
            isStarted = true;
            enableOrDisableButtons();
            itemNumber = 0;
            arTempUnknownVerbs.clear();
            VerbsActivity.numberOfTestsInSession++;
            printQuestion();
        } else {
            // It means no forms were checked:
            GUITools.alert(context, context.getString(R.string.warning), context.getString(R.string.no_forms_were_chosen));
        } // end if no forms were chosen.
    } // end startNow() method.

    // A method which fills the aCurForms array with a verb:
    private void getCurForms() {
        String sql;

        int unknownVerbId = (int) getUnknownVerbs();
        if (unknownVerbId == 0) {
            // It means there is no unknown verbs saved:

            // If all are consumed, value 1, make all 0:
            sql = "SELECT COUNT(repet) FROM verbe WHERE repet=0";
            Cursor countCursor = mDbHelper.queryData(sql);
            countCursor.moveToFirst();
            int unconsumedVerbs = countCursor.getInt(0);
            countCursor.close();
            /*
             * If unconsumedVerbs is 0, all values are 1, make all REPET values
             * as 0:
             */
            if (unconsumedVerbs <= 0) {
                GUITools.beep();
                sql = "UPDATE verbe SET repet=0 WHERE repet=1";
                mDbHelper.updateData(sql);
            } // end if is a reset of consumed verbs.

            // An SQL for random query:
            sql = "SELECT * FROM verbe WHERE repet=0 ORDER BY random() LIMIT 1";
        } // end if there is no an unknown verb saved.
        else {
            // If there where an unknown verb:
            sql = "SELECT * FROM verbe WHERE id=" + unknownVerbId;
        } // end if it's from unknown verbs.

        Cursor cursor = mDbHelper.queryData(sql);
        /*
         * We keep in mind the last id to see if it's necessary to insert it
         * into unknown ones:
         */
        lastVerbId = cursor.getInt(0);

        // Make "REPET" column to be as value 1 for last id if is from DB, not
        // unknown verbs:
        if (unknownVerbId == 0) {
            sql = "UPDATE verbe SET repet=1 WHERE id=" + cursor.getInt(0);
            mDbHelper.updateData(sql);
        } // end if isn't from unknown verbs.

        for (int i = 1; i < aCurForms.length; i++) {
            aCurForms[i] = cursor.getString(i);
        } // end for fill the aCurForms array.
        cursor.close();
    } // end getCurForms() method.

    // A method which prints a verb in quiz:
    private void printQuestion() {
        SoundPlayer.playSimple(context, "new_quiz");
        // First clear all layouts:
        clearLL();
        // Increase the item number.
        // This is the actual number of question:
        itemNumber = (byte) (itemNumber + 1);
        // Add the question number in llTop:
        String title = String.format(context.getString(R.string.item_title_in_quiz), "" + itemNumber, "" + itemsLimit);
        TextView titleItemTV = createTextView(title, textSize + 2);
        llTop.addView(titleItemTV);

        // We need an array with 5 items, for each form and translation.
        // 0 index is null:
        getCurForms();
        /*
         * Now we must add the verb forms in the llMain, those will be TextViews
         * or TextViews editable.
         */
        /*
         * We need an array of 5 TextViews. The first one won't exist. The array
         * is declared globally in this class.
         */
        // For padding at linear layouts:
        int mPadding = MainActivity.mPaddingDP;
        for (int i = 1; i < aTvs.length; i++) {
            final int which = i;
            LinearLayout llRow = new LinearLayout(context);
            llRow.setOrientation(LinearLayout.HORIZONTAL);
            llRow.setGravity(Gravity.START);
            llRow.setPadding(mPadding * paddingMultiplier, mPadding, mPadding, mPadding);

            // A label with form number or translate:
            TextView tvLabel;
            if (i < 4) {
                tvLabel = createTextView("" + i + ". ", textSize + 1);
            } else {
                tvLabel = createTextView("T. ", textSize + 1);
            }
            llRow.addView(tvLabel);

            // The text view for a row, a form:
            final String verbForm;
            // If must be shown or not:
            if (i < bObjects.length && arForms.contains(bObjects[i])) {
                verbForm = context.getString(R.string.text_unfilled);
            } else {
                verbForm = aCurForms[i];
            }
            TextView tv = createTextView(verbForm, textSize);
            // If must contain contentDescription or not::
            if (i < bObjects.length && arForms.contains(bObjects[i])) {
                // It means it is unfilled form:
                tv.setContentDescription(String.format(context.getString(R.string.content_description_unfilled), aVerbForms[i]));
            }

            tv.setPadding(mPaddingDP, 0, 0, 0);
            // If is a chosen form, add a listener:
            if (i < bObjects.length && arForms.contains(bObjects[i])) {
                tv.setOnClickListener(view -> formTVClicked(which));
                // End add listener for tap on verb form.
            } // end if form was chosen.
            else {
                // If is a shown form, say it on tap if is not the translation:
                tv.setOnClickListener(view -> {
                    if (which < 4) {
                        speak.sayUsingLanguage(verbForm, true);
                    }
                });
                // End add listener for tap on verb form.

                // For a long click, spell the result:
                tv.setOnLongClickListener(view -> {
                    speak.sayUsingLanguage("", true);
                    speak.spellUsingLanguage(verbForm);

                    return true;
                });
                // End add listener for long click on a result.

            } // end if is a shown form, not to fill.
            aTvs[i] = tv;
            // Make a LayoutParams to fill parent:
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llRow.addView(aTvs[i], tvParams);

            // Make a LayoutParams for llRos to fill parent:
            LinearLayout.LayoutParams llRowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llMain.addView(llRow, llRowParams);
        } // end for create 2 * 4 TextViews in 4 LinearLayouts.

        // A RelativeLayout for next button and progress bar:
        RelativeLayout lr = new RelativeLayout(context);
        lr.setPadding(mPadding, mPadding, mPadding, mPadding);

        LinearLayout llNextAndProgress = new LinearLayout(context);
        llNextAndProgress.setOrientation(LinearLayout.VERTICAL);
        llNextAndProgress.setGravity(Gravity.END);

        // A LayoutParams for children added:
        // This child will be the LinearLayout above:
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        // We add in llNextAndProgress the next button:
        // See if is next or finish:
        String textButton;
        if (itemNumber < itemsLimit) {
            textButton = context.getString(R.string.bt_next_item);
        } else {
            textButton = context.getString(R.string.bt_finish_quiz);
        }
        btNext = createButton(textButton, textSize);
        btNext.setOnClickListener(view -> actionsOnNextPressed());
        // End add listener for tap on btNext.
        btNext.setEnabled(false);
        // A LayoutParams for this button:
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llNextAndProgress.addView(btNext, llParams);

        // Create also a progress bar:
        // Calculate current percentage of the text:
        int curPercentage = (itemNumber - 1) * 10;
        ProgressBar pb = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        pb.setProgress(curPercentage);
        pb.setContentDescription(String.format(context.getString(R.string.quiz_progress_bar), "" + curPercentage));
        // Add the progress bar to LinearLayout.
        // A LayoutParams for this button:
        LinearLayout.LayoutParams llParamsPB = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llNextAndProgress.addView(pb, llParamsPB);

        // Add the LinearLayout into RelativeLayout:
        lr.addView(llNextAndProgress, lp);

        // Add the RelativeLayout into llMain:
        // A LayoutParams for RelativeLayout to see how to add in llMain:
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        llMain.addView(lr, rlp);
    } // end showQuestion() method.

    // A method which is called when clicking next button:
    private void actionsOnNextPressed() {
        checkLastAnswer();
        if (itemNumber < itemsLimit) {
            printQuestion();
        } else {
            printResults();
        }
    } // end actionsOnNextPressed() method.

    // A method which is called when a formTV is clicked:
    private void formTVClicked(final int which) {
        aTvs[which].setText("");
        aTvs[which].setCursorVisible(true);
        aTvs[which].setFocusable(true);
        aTvs[which].setFocusableInTouchMode(true);
        aTvs[which].requestFocus();
        aTvs[which].setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        aTvs[which].setContentDescription("");
        btNext.setEnabled(true);
        // Add also an action listener for editing:
        aTvs[which].setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Play a sound when the keyboard is hidden:
                SoundPlayer.playSimple(context, "written");
                // Fill again text of not filled:
                String temp = st.replaceCharacters(aTvs[which].getText().toString());
                if (temp.equals("")) {
                    // Fill again the text and content description:
                    aTvs[which].setText(context.getString(R.string.text_unfilled));
                    aTvs[which].setContentDescription(String.format(context.getString(R.string.content_description_unfilled), aVerbForms[which]));
                    btNext.setEnabled(false);
                } else {
                    /*
                     * Something was written, we set text again with
                     * characters replaced if needed:
                     */
                    aTvs[which].setText(temp);
                }
            }
            return false;
        });
        // End add action listener for the IME done button of the keyboard..

        // Show also the keyboard for this TextView:
        InputMethodManager imm = (InputMethodManager) context.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(aTvs[which], InputMethodManager.SHOW_IMPLICIT);
        // imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    } // end tvFormClicked() method.

    // This method is called in nextQuestion method:
    private void checkLastAnswer() {
        boolean isError = false;
        for (int i = 1; i < aTvs.length; i++) {
            if (i < bObjects.length && arForms.contains(bObjects[i])) {
                /*
                 * We check if if the strings are equals in the TextViews filled
                 * and in the aCurForms:
                 */
                boolean isTempError = true;
                String[] allTempForms = aCurForms[i].split("/");
                for (String tempForm : allTempForms) {
                    if (tempForm.equalsIgnoreCase(aTvs[i].getText().toString())) {
                        isTempError = false;
                        break;
                    }
                } // end for.

                if (isTempError) {
                    numberOfErrors++; // increase the numberOfErrors.
                    isError = true;
                    String resItemError = context.getString(R.string.final_item_error);
                    // Format the string:
                    String tempMessage = String.format(resItemError, "" + itemNumber, aVerbForms[i], aCurForms[1], aCurForms[i], aTvs[i].getText().toString());
                    mHistory.add(tempMessage);
                } // end if strings are not equals, error.
            } // end if is a form to check.
        } // end for going through all forms.

        if (isError) {
            arTempUnknownVerbs.add((short) lastVerbId);
        }
    } // end checkLastAnswer() method.

    // A method which prints final results:
    private void printResults() {
        // Before printing, we add the last errors into unknown verbs:
        for (int i = 0; i < arTempUnknownVerbs.size(); i++) {
            saveUnknownVerbs((short) arTempUnknownVerbs.get(i));
        } // end for add last errors in unknown verbs.

        SoundPlayer.playSimple(context, "new_event");
        // Clear llTop and llMain:
        clearLL(); // a method in the super class.

        // Calculate the final mark:
        int nr = arForms.size();
        int points = itemsLimit * nr - numberOfErrors;
        double mark = (double) points / (double) nr;
        mark = Math.round(mark * 100.0) / 100.0;

        /*
         * Call the method which inserts a new mark, calculates the average and
         * other things:
         */
        insertNewTestFinished(mark);

        // Make a text view for title, the mark:
        // The string form final mark:
        String markMessage = String.format(context.getString(R.string.final_mark), "" + mark);
        TextView tv = createTextView(markMessage, textSize + 1);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        // A LayoutParams for weight for text view in the llTop, to be 1F::
        LinearLayout.LayoutParams tvTopParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        tvTopParams.weight = 1.0f;
        // Add the tvTop into llTop:
        llTop.addView(tv, tvTopParams);

        // Add the number of errors message in history:
        String errorsTitle;
        if (numberOfErrors > 0) {
            // First take the corresponding plural resource:
            Resources res = context.getResources();
            errorsTitle = res.getQuantityString(R.plurals.tv_number_of_mistakes, numberOfErrors, numberOfErrors);
        } else {
            // No errors found:
            errorsTitle = context.getString(R.string.tv_you_had_no_mistakes);
        }

        mHistory.addAsFirst(errorsTitle);

        // Add now the ScrollView with errors:
        ScrollView sv = mHistory.getSVHistory();

        // Add now the SV into the main LinearLayout:
        llMain.addView(sv);

        /*
         * At this moment we can set isStarted to false and re-enable buttons in
         * the upper part of the screen:
         */
        isStarted = false;
        enableOrDisableButtons();

    } // end printResults() method.

    // A method to make the average:
    private void insertNewTestFinished(double mark) {
        // Increase the number of tests.
        VerbsActivity.numberOfTests++;

        // Add the last mark to sumOfMarks:
        VerbsActivity.sumOfMarks = VerbsActivity.sumOfMarks + mark;

        // Calculate the new general average:
        VerbsActivity.average = VerbsActivity.sumOfMarks / (double) VerbsActivity.numberOfTests;

        // Save all these new values:
        Settings set = new Settings(context);
        set.saveIntSettings("numberOfTests", VerbsActivity.numberOfTests);
        set.saveDoubleSettings("sumOfMarks", VerbsActivity.sumOfMarks);
        set.saveDoubleSettings("average", VerbsActivity.average);

        // Add also the last mark to lastXMarks:
        VerbsActivity.saveLastXMarks(context, mark);

        // Add now in DataBase the test finished:
        // We need also the test type as a string:
        StringBuilder testType = new StringBuilder();
        for (int i = 0; i < arForms.size(); i++) {
            testType.append(arForms.get(i));
        } // end for.
        Statistics stats = new Statistics(context);
        stats.postTestFinished(MainActivity.myAccountName, testType.toString(), mark);
    } // end insertNewTestFinished() method.

    // Method to save an id for a unknown verb:
    private void saveUnknownVerbs(short newVerb) {
        // Make an ArrayList of shorts from SharedPrefferences:
        ArrayList<Short> arUnknownVerbs = new ArrayList<>();

        // Get the string from SharedPrefferences:
        Settings set = new Settings(context);
        String unknownVerbs = set.getStringSettings("unknownVerbs");

        // Only if is not null, there is something there:
        if (unknownVerbs != null && unknownVerbs.length() > 0) {
            String[] aTemp = unknownVerbs.split("\\|");
            for (String s : aTemp) {
                arUnknownVerbs.add(Short.parseShort(s));
            } // end for adding into arUnknownVerbs.
        }

        // Add also the new value, the newVerb:
        arUnknownVerbs.add(newVerb);

        // Save now again the string:
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arUnknownVerbs.size(); i++) {
            sb.append(arUnknownVerbs.get(i));
            if (i < arUnknownVerbs.size() - 1) {
                sb.append("|");
            }
        } // end for.
        set.saveStringSettings("unknownVerbs", sb.toString());
    } // end saveUnknownVerbs() method.

    // A method to get a random verb from those unknown:
    private short getUnknownVerbs() {
        // Make an ArrayList of shorts from SharedPrefferences:
        ArrayList<Short> arUnknownVerbs = new ArrayList<>();

        // Get the string from SharedPrefferences:
        Settings set = new Settings(context);
        String unknownVerbs = set.getStringSettings("unknownVerbs");

        if (unknownVerbs != null && unknownVerbs.length() > 0) {
            String[] aTemp = unknownVerbs.split("\\|");
            for (String s : aTemp) {
                arUnknownVerbs.add(Short.parseShort(s));
            } // end for adding into arUnknownVerbs.
        } // end if there is something saved for unknown verbs.

        short idToReturn = 0;
        if (arUnknownVerbs.size() > 0) {
            int rand = GUITools.random(0, arUnknownVerbs.size() - 1);
            idToReturn = arUnknownVerbs.get(rand);
            // Remove the last given verb fromunknownVerbs:
            arUnknownVerbs.remove(rand);

            // Save now again the string:
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arUnknownVerbs.size(); i++) {
                sb.append(arUnknownVerbs.get(i));
                if (i < arUnknownVerbs.size() - 1) {
                    sb.append("|");
                }
            } // end for.
            set.saveStringSettings("unknownVerbs", sb.toString());
        } // end if there is at least one unknown verb.

        return idToReturn;
    } // end getUnknownVerbs() method.

} // end class QuizIrregularVerbs.
