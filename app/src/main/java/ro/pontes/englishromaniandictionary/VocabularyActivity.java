package ro.pontes.englishromaniandictionary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class VocabularyActivity extends Activity implements
        OnItemSelectedListener {

    private DBAdapter2 mDbHelper;
    private SpeakText speak;

    private int numberOfRecords = 0;
    private int numberOfCategories = 0;
    private String curCategoryName = "%";
    private int curSpinnerPosition = 0;
    private File file = null;
    private StringTools st = null;

    private final Context mFinalContext = this;

    /*
     * We need a global variable TextView for a result. A value will be
     * attributed in onCreateContextMenu, and will be read in
     * onContextItemSelected:
     */
    private TextView tvResultForContext;

    /*
     * We need a global alertToShow as alert to be able to dismiss it when
     * needed and other things:
     */
    private AlertDialog alertToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * We charge different layouts depending of the premium status or
         * android TV:
         */
        if (MainActivity.isTV) {
            if (MainActivity.isPremium) {
                setContentView(R.layout.activity_vocabulary_premium_tv);
            } else {
                setContentView(R.layout.activity_vocabulary_tv);
            }
        } else {
            // No if is not a TV:
            if (MainActivity.isPremium) {
                setContentView(R.layout.activity_vocabulary_premium);
            } else {
                setContentView(R.layout.activity_vocabulary);
            }
            // Set the orientation to be portrait if needed:
            if (MainActivity.isOrientationBlocked) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } // end if isOrientationBlocked is true.
        } // end if else, not an Android TV.
        // end charging the correct layout.

        st = new StringTools(this);

        // Start things for our database:
        mDbHelper = new DBAdapter2(this);
        mDbHelper.createDatabase();
        mDbHelper.open();

        /*
         * Update the number of words and categories message, under drop down.
         * It is important to call this method first because it fills also a
         * global variable with number of records.
         */
        updateWelcomeMessage();

        // Call the method which fills the spinner:
        if (numberOfRecords > 0) {
            updateSpinner();
        } // end if there is at least one record.

        // Call the method to show banner if is not premium:
        if (!MainActivity.isPremium) {
            adMobSequence();
        }

        // To keep screen awake:
        if (MainActivity.isWakeLock) {
            getWindow()
                    .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

        numberOfRecords = getNumberOfRecords();

        // The number of categories in DB:
        String sql = "SELECT COUNT(*) AS total FROM sectiuni";
        Cursor cursor = mDbHelper.queryData(sql);
        numberOfCategories = cursor.getInt(0);
        cursor.close();

        TextView tv = (TextView) findViewById(R.id.tvNumberOfRecords);
        String message = "";
        if (numberOfRecords > 0) {
            // Calculate also the average:
            double average = (double) numberOfRecords
                    / (double) numberOfCategories;
            average = GUITools.round(average, 2);

            message = String.format(
                    getString(R.string.tv_welcome_vocabulary_message), ""
                            + numberOfCategories, "" + numberOfRecords, ""
                            + average);
        } else {
            message = getString(R.string.no_records_yet);
        }
        tv.setText(message);
        tv.setFocusable(true);
    } // end updateWelcomeMessage() method.

    // Update the drop down list:
    private void updateSpinner() {

        // Get the categories and number of records in them:
        String sql = "SELECT * FROM sectiuni ORDER BY nume";
        Cursor cursor = mDbHelper.queryData(sql);

        // Create a delimited string:

        StringBuilder sb = new StringBuilder(
                getString(R.string.in_spinner_choose_category));
        sb.append("|");

        // Update again numberOfRecords in case a record was deleted:
        numberOfRecords = getNumberOfRecords();

        Resources res = getResources();

        sb.append(res.getQuantityString(
                R.plurals.number_of_all_words_in_vocabulary, numberOfRecords,
                numberOfRecords));

        cursor.moveToFirst();
        do {
            sb.append("|");
            String categoryName = cursor.getString(1);
            // Get number of words in this category:
            Cursor cursor2 = mDbHelper
                    .queryData("SELECT COUNT(*) FROM vocabular WHERE idSectiune = '"
                            + cursor.getInt(0) + "'");
            int nr = cursor2.getInt(0);
            cursor2.close();
            String catTitle = String.format(
                    getString(R.string.dropdown_category), categoryName, res
                            .getQuantityString(
                                    R.plurals.number_of_words_in_category, nr,
                                    nr));
            sb.append(MyHtml.fromHtml(catTitle));
        } while (cursor.moveToNext());
        // end do ... while.
        cursor.close();

        Spinner dropdown = (Spinner) findViewById(R.id.spinnerChoose);
        String[] items = sb.toString().split("\\|");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
        dropdown.setSelection(curSpinnerPosition);
    } // end updateSpinner() method.

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        curSpinnerPosition = position;
        if (position == 0) {
            // Do nothing, nothing was selected.
            curCategoryName = "%";
        } else if (position == 1) {
            createList("%");
        } else {
            String chosen = parent.getItemAtPosition(position).toString();
            String[] aChosen = chosen.split(" - ");
            String chosenCategoryName = aChosen[0];
            createList(chosenCategoryName);
        } // end if position is greater than 1, a category.
    } // end onItemSelected() implemented method.

    // The method which writes the list of records:
    private void createList(String categoryName) {
        curCategoryName = new String(categoryName);
        categoryName = st.realEscapeString(categoryName);
        // Hide the bottom layout, AdMob:
        hideAdMob(true);

        // Clear the previous content of the llList layout:
        LinearLayout ll = (LinearLayout) findViewById(R.id.llList);
        ll.removeAllViews();

        // Determine the id of the section:
        String idSection = "%";
        if (!categoryName.equals("%")) {
            // Query from DB the idSection number:
            String sqlTemp = "SELECT id FROM sectiuni WHERE nume='"
                    + categoryName + "';";
            Cursor cur = mDbHelper.queryData(sqlTemp);
            idSection = cur.getString(0);
            cur.close();
        } // end get idSection number as string.

        // Create TextViews for each record:
        int mPaddingDP = MainActivity.mPaddingDP;
        TextView tv;
        Cursor cursor = mDbHelper
                .queryData("SELECT * FROM vocabular WHERE idSectiune LIKE '"
                        + idSection
                        + "' ORDER BY termen COLLATE NOCASE, explicatie COLLATE NOCASE");
        cursor.moveToFirst();
        String toFormat = getString(R.string.tv_word_and_explanation);
        do {
            tv = new TextView(this);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
            tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
            // Format the string:
            String curWord = cursor.getString(2);
            String curExplanation = cursor.getString(3);
            String tvText = String.format(toFormat, curWord,
                    MainActivity.weSeparator, curExplanation);
            CharSequence tvSeq = MyHtml.fromHtml(tvText);
            tv.setText(tvSeq);
            tv.setFocusable(true);
            // No add an listener for short tap:
            // Get also the tip, the direction EN_RO or RO_EN:
            final int direction = cursor.getInt(5);
            // Determine the English part to be spoken or spelled:
            final String englishPart;
            if (direction == 0) {
                englishPart = curWord;
            } else {
                englishPart = curExplanation;
            }

            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    speak.sayUsingLanguage(englishPart, true);
                }
            });
            // End add listener for tap on verb form.

            // For a long click, add a context menu:
            registerForContextMenu(tv);

            ll.addView(tv);
        } while (cursor.moveToNext());
    }// end createList() method.

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    } // end onNothingSelected() implemented method.

    // The method to generate the AdMob sequence:
    private void adMobSequence() {

    } // end adMobSequence().

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vocabulary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.mnuSectionAddNew) {
            addRecord();
        } else if (id == R.id.mnuSectionInformation) {
            showSectionInformation();
        } else if (id == R.id.mnuSectionRename) {
            renameCategory();
        } else if (id == R.id.mnuImportPredefined) {
            importPredefinedCategories("predefined_categories");
        } else if (id == R.id.mnuDeleteSection) {
            // deleteEntireCategory();
        } else if (id == R.id.mnuDeleteAll) {
            deleteEntireVocabulary();
        } else if (id == R.id.mnuGoToDictionary) {
            this.finish();
            GUITools.goToDictionary(this);
        } else if (id == R.id.mnuIrregularVerbs) {
            this.finish();
            GUITools.goToIrregularVerbs(this);
        } else if (id == R.id.mnuHelp) {
            GUITools.showHelp(this);
        } // end if Help is chosen in main menu.
        else if (id == R.id.mnuAboutDialog) {
            GUITools.aboutDialog(this);
        } // end if about dictionary is chosen in main menu.
        else if (id == R.id.mnuRate) {
            GUITools.showRateDialog(this);
        } // end if rate option was chosen in menu.

        return super.onOptionsItemSelected(item);
    } // end onOptionsItemSelected() method.

    // The implementations for context menu:
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(getString(R.string.cm_vocabulary_title));
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.vocabulary_context_menu, menu);

        // We store globally the text view clicked longly:
        tvResultForContext = (TextView) v;
    } // end onCreateContextMenu() method.

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // First we take the text from the longly clicked result:
        String result = tvResultForContext.getText().toString();
        String[] aResult = result.split(MyHtml.fromHtml(MainActivity.weSeparator).toString());
        String w = st.cleanString(aResult[0]);
        String e = st.cleanString(aResult[1]);

        // Get from database the record for this w:
        String sql = "SELECT * FROM vocabular where termen = '"
                + st.realEscapeString(w) + "' AND explicatie = '"
                + st.realEscapeString(e) + "'";

        Cursor cur = mDbHelper.queryData(sql);
        int direction = cur.getInt(5);
        String englishPart = "";
        if (direction == 0) {
            englishPart = cur.getString(2);
        } else {
            englishPart = cur.getString(3);
        }
        int curTime = cur.getInt(4);
        int idSection = cur.getInt(1);
        int idRecord = cur.getInt(0);
        cur.close();
        @SuppressWarnings("unused")
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        switch (item.getItemId()) {

            case R.id.cmSearchResult:
                goToDictionaryAndSearch(w, direction);
                return true;

            case R.id.cmSpeakResult:
                speak.sayUsingLanguage(englishPart, true);
                return true;
            case R.id.cmSpellResult:
                speak.sayUsingLanguage("", true);
                speak.spellUsingLanguage(englishPart);
                return true;
            case R.id.cmCopyResult:
                GUITools.copyIntoClipboard(this, result);
                return true;
            case R.id.cmCopyWord:
                GUITools.copyIntoClipboard(this, w);
                return true;
            case R.id.cmCopyExplanation:
                GUITools.copyIntoClipboard(this, e);
                return true;
            case R.id.cmVocabularyEdit:
                editRecord(idRecord);
                return true;
            case R.id.cmVocabularyDelete:
                deleteRecord(w, e);
                return true;
            case R.id.cmVocabularyProperties:
                showVocabularyProperties(w, e, idSection, direction, curTime);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    } // end selected item in context menu.

    // End context menu implementation.

    // A method to change category name:
    private void renameCategory() {
        // Make a new context:
        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);

        // Only if a category was chosen:
        if (!curCategoryName.equals("%")) {
            // Get the strings to make an alert:
            String tempTitle = getString(R.string.title_rename_section);

            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);

            // A LayoutParams to add the edit texts:
            LinearLayout.LayoutParams etlp = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            etlp.setMargins(0, MainActivity.mPaddingDP * 3, 0, 0);

            // A TextView for body of this action:
            TextView tv = new TextView(context);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
            String message = MyHtml.fromHtml(
                    String.format(getString(R.string.message_rename_section),
                            curCategoryName)).toString();
            tv.setText(message);
            tv.setFocusable(true);
            ll.addView(tv, etlp);

            // EditText for new section name:
            final EditText etName = createEditText();
            etName.setHint(getString(R.string.hint_write_new_section_name));
            etName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                    | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            etName.setImeOptions(EditorInfo.IME_ACTION_DONE);
            etName.setOnEditorActionListener(new OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId,
                                              KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        String newName = etName.getText().toString();

                        if (saveRenameSection(newName)) {
                            alertToShow.dismiss();
                        }
                    } // end if DONE key was pressed.
                    return false;
                }
            });
            // End add action listener.
            ll.addView(etName, etlp);

            AlertDialog.Builder alert = new AlertDialog.Builder(context)
                    .setTitle(tempTitle)
                    .setView(ll)
                    .setPositiveButton(R.string.rename,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    String newName = etName.getText()
                                            .toString();
                                    saveRenameSection(newName);
                                } // end if save button was pressed.
                            }).setNegativeButton(android.R.string.cancel, null);

            alertToShow = alert.create();
            alertToShow.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            alertToShow.show();
        } else {
            GUITools.alert(this, getString(R.string.warning),
                    getString(R.string.warning_no_category_chosen));
        }
    } // end editRecord() method.

    // A method to rename effectively current section:
    private boolean saveRenameSection(String name) {
        boolean toReturn = false;
        // Save now the new record:
        // First of all, check if edit text has text:
        String newname = name.trim();

        if (newname.length() >= 2) {
            // The new name must not be already in DB:
            if (!fieldExists("sectiuni", "nume", newname)) {
                // Add it effectively:
                // Determine current sectionId:
                String sql = "SELECT id FROM sectiuni WHERE nume = '"
                        + st.realEscapeString(curCategoryName) + "'";
                Cursor cur = mDbHelper.queryData(sql);
                int sectionId = cur.getInt(0);

                // Update:
                sql = "UPDATE sectiuni SET nume='"
                        + st.realEscapeString(newname) + "' WHERE id="
                        + sectionId + ";";
                mDbHelper.updateData(sql);
                SoundPlayer.playSimple(mFinalContext, "hand_writting");

                // Re-charge this section:
                curCategoryName = st.realEscapeString(newname);
                updateSpinner();
                createList(curCategoryName);
                toReturn = true;
            } // end if name section doesn't exist, all is good..
            else {
                GUITools.alert(
                        mFinalContext,
                        getString(R.string.warning),
                        getString(R.string.this_vocabulary_section_already_exists_rename));
            } // end if section name already exist.
        } // end if new name edit has text.
        else {
            GUITools.alert(mFinalContext, getString(R.string.warning),
                    getString(R.string.no_texts_for_new_section_name));
        } // end if edit text hasn't text.
        return toReturn;
    } // end saveRenameSection() method.

    // A method to add a word:
    private void addRecord() {
        // Make a new context:
        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);

        // Only if a category was chosen:
        if (!curCategoryName.equals("%")) {
            // Get the strings to make an alert:
            String tempTitle = getString(R.string.title_add_in_section);

            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);

            // A LayoutParams to add the edit texts:
            LinearLayout.LayoutParams etlp = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            etlp.setMargins(0, MainActivity.mPaddingDP * 3, 0, 0);

            // A TextView for body of this action:
            TextView tv = new TextView(context);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
            String message = MyHtml.fromHtml(
                    String.format(getString(R.string.message_add_in_section),
                            curCategoryName)).toString();
            tv.setText(message);
            tv.setFocusable(true);
            ll.addView(tv, etlp);

            // EditText for word:
            final EditText etWord = createEditText();
            etWord.setHint(getString(R.string.hint_add_new_word));
            etWord.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
                    | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            etWord.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            ll.addView(etWord, etlp);

            // EditText for explanation:
            final EditText etExplanation = createEditText();
            etExplanation.setHint(getString(R.string.hint_add_new_explanation));
            etExplanation
                    .setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
                            | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            etExplanation.setImeOptions(EditorInfo.IME_ACTION_DONE);
            etExplanation
                    .setOnEditorActionListener(new OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId,
                                                      KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                String newWord = etWord.getText().toString();
                                String newExplanation = etExplanation.getText()
                                        .toString();
                                if (saveAdd(newWord, newExplanation)) {
                                    alertToShow.dismiss();
                                }
                            } // end if DONE key was pressed.
                            return false;
                        }
                    });
            // End add action listener.
            ll.addView(etExplanation, etlp);

            AlertDialog.Builder alert = new AlertDialog.Builder(context)
                    .setTitle(tempTitle)
                    .setView(ll)
                    .setPositiveButton(R.string.save,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    String newWord = etWord.getText()
                                            .toString();
                                    String newExplanation = etExplanation
                                            .getText().toString();
                                    saveAdd(newWord, newExplanation);
                                } // end if save button was pressed.
                            }).setNegativeButton(android.R.string.cancel, null);

            alertToShow = alert.create();
            alertToShow.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            alertToShow.show();
        } else {
            GUITools.alert(this, getString(R.string.warning),
                    getString(R.string.warning_no_category_chosen));
        }
    } // end editRecord() method.

    // A method to save effectively the addition from method above:
    private boolean saveAdd(String word, String explanation) {
        boolean toReturn = false;
        // Save now the new record:
        // First of all, check if both edit text have text:
        String newWord = (MyHtml.fromHtml(word).toString()).trim();
        String newExplanation = (MyHtml.fromHtml(explanation).toString())
                .trim();
        if (newWord.length() >= 2 && newExplanation.length() >= 2) {

            // The new record must not be already in DB:
            if (!recordExistsInVocabulary(st.realEscapeString(newWord),
                    st.realEscapeString(newExplanation))) {
                // Add it effectively:
                long curTime = GUITools.getTimeInSeconds();
                // Determine current sectionId:
                String sql = "SELECT id FROM sectiuni WHERE nume = '"
                        + st.realEscapeString(curCategoryName) + "'";
                Cursor cur = mDbHelper.queryData(sql);
                int sectionId = cur.getInt(0);

                // Insert into:
                sql = "INSERT INTO vocabular (idSectiune, termen, explicatie, data) VALUES ('"
                        + sectionId
                        + "', '"
                        + st.realEscapeString(newWord)
                        + "', '"
                        + st.realEscapeString(newExplanation)
                        + "', '"
                        + curTime + "')";
                mDbHelper.insertData(sql);
                SoundPlayer.playSimple(mFinalContext, "hand_writting");

                /*
                 * Post in statistics about this insert of a word in DB:
                 */
                Statistics stats = new Statistics(this);
                stats.postStats("30", 1);

                // Re-charge this section:
                updateSpinner();
                createList(curCategoryName);
                toReturn = true;
            } // end if record doesn't exist, the best scenario.
            else {
                GUITools.alert(
                        mFinalContext,
                        getString(R.string.warning),
                        getString(R.string.this_record_already_exists_in_vocabulary));
            } // end if record already exist.
        } // end if both edit text have texts.
        else {
            GUITools.alert(mFinalContext, getString(R.string.warning),
                    getString(R.string.no_texts_for_edit));
        } // end if edit text haven't text.
        return toReturn;
    } // end save new add Edit() method.

    // A method to edit a word:
    private void editRecord(int idRecord) {

        // Make a new context:
        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);

        String sql = "SELECT * FROM vocabular where id = '" + idRecord + "'";
        Cursor cur = mDbHelper.queryData(sql);
        final int idRecordTemp = cur.getInt(0);
        final String word = cur.getString(2);
        final String explanation = cur.getString(3);
        cur.close();

        // Get the strings to make an alert:
        String tempTitle = getString(R.string.title_edit_record_in_vocabulary);

        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        // A LayoutParams to add the edit texts:
        LinearLayout.LayoutParams etlp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        etlp.setMargins(0, MainActivity.mPaddingDP * 3, 0, 0);

        // EditText for word:
        final EditText etWord = createEditText();
        etWord.setText(word);
        etWord.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
                | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        etWord.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        ll.addView(etWord, etlp);

        // EditText for explanation:
        final EditText etExplanation = createEditText();
        etExplanation.setText(explanation);
        etExplanation.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
                | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        etExplanation.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etExplanation.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String newWord = etWord.getText().toString();
                    String newExplanation = etExplanation.getText().toString();
                    if (saveEdit(newWord, newExplanation, idRecordTemp)) {
                        alertToShow.dismiss();
                    }
                } // end if DONE key was pressed.
                return false;
            }
        });
        // End add action listener.
        ll.addView(etExplanation, etlp);

        // Ramona
        AlertDialog.Builder alert = new AlertDialog.Builder(context)
                .setTitle(tempTitle)
                .setView(ll)
                .setPositiveButton(R.string.save,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String newWord = etWord.getText().toString();
                                String newExplanation = etExplanation.getText()
                                        .toString();
                                saveEdit(newWord, newExplanation, idRecordTemp);
                            } // end if save button was pressed.
                        }).setNegativeButton(android.R.string.cancel, null);

        alertToShow = alert.create();
        alertToShow.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertToShow.show();
    } // end editRecord() method.

    // A method to save effectively the edit from method above:
    private boolean saveEdit(String word, String explanation, int idRecordTemp) {
        boolean toReturn = false;
        // Save now the new record:
        // First of all, check if both edit text have text:
        String newWord = (MyHtml.fromHtml(word).toString()).trim();
        String newExplanation = (MyHtml.fromHtml(explanation).toString())
                .trim();
        if (newWord.length() >= 2 && newExplanation.length() >= 2) {

            // The new record must not be already in DB:
            if (!recordExistsInVocabulary(st.realEscapeString(newWord),
                    st.realEscapeString(newExplanation))) {
                // Edit it effectively:
                String sql = "UPDATE vocabular SET termen = '"
                        + st.realEscapeString(newWord) + "', explicatie = '"
                        + st.realEscapeString(newExplanation)
                        + "' WHERE id = '" + idRecordTemp + "'";
                mDbHelper.updateData(sql);
                SoundPlayer.playSimple(mFinalContext, "hand_writting");

                /*
                 * Post in statistics about this insert of a word in DB:
                 */
                Statistics stats = new Statistics(this);
                stats.postStats("31", 1);

                // Re-charge this section:
                createList(curCategoryName);
                toReturn = true;
            } // end if record doesn't exist, the best scenario.
            else {
                GUITools.alert(
                        mFinalContext,
                        getString(R.string.warning),
                        getString(R.string.this_record_already_exists_in_vocabulary));
            } // end if record already exist.
        } // end if both edit text have texts.
        else {
            GUITools.alert(mFinalContext, getString(R.string.warning),
                    getString(R.string.no_texts_for_edit));
        } // end if edit text haven't text.
        return toReturn;
    } // end save Edit() method.

    // A method which check if a field exists in a database:
    private boolean fieldExists(String table, String field, String text) {
        boolean exists = false;
        DBAdapter2 mDbHelperTemp = new DBAdapter2(this);
        mDbHelperTemp.createDatabase();
        mDbHelperTemp.open();

        String sql = "SELECT COUNT(*) AS total FROM " + table + " WHERE "
                + field + " = '" + text + "';";
        Cursor cur = mDbHelperTemp.queryData(sql);
        int count = cur.getInt(0);
        cur.close();
        mDbHelperTemp.close();

        if (count > 0) {
            exists = true;
        }

        return exists;
    } // end fieldExists() method.

    // A method which checks if this entry already exists in DB:
    private boolean recordExistsInVocabulary(String word, String explanation) {
        // This method exits also in MainActivity class.
        boolean exists = false;
        DBAdapter2 mDbHelperTemp = new DBAdapter2(this);
        mDbHelperTemp.createDatabase();
        mDbHelperTemp.open();

        String sql = "SELECT COUNT(*) AS total FROM vocabular WHERE termen='"
                + word + "' AND explicatie='" + explanation + "'";
        Cursor cur = mDbHelperTemp.queryData(sql);
        int count = cur.getInt(0);
        cur.close();
        mDbHelperTemp.close();

        if (count > 0) {
            exists = true;
        }

        return exists;
    } // end recordExists() method.

    // A method to delete a word:
    private void deleteRecord(final String word, final String explanation) {
        String sql = "SELECT * FROM vocabular where termen='"
                + st.realEscapeString(word) + "' AND explicatie ='"
                + st.realEscapeString(explanation) + "'";
        Cursor cur = mDbHelper.queryData(sql);
        final int idSectionTemp = cur.getInt(1);
        cur.close();

        // Get the strings to make an alert:
        String tempTitle = getString(R.string.title_delete_record_in_vocabulary);
        String tempBody = String.format(
                getString(R.string.body_delete_record_in_vocabulary), word);
        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);
        new AlertDialog.Builder(context)
                .setTitle(tempTitle)
                .setMessage(MyHtml.fromHtml(tempBody))
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                                // Delete now the record:
                                String sql = "DELETE FROM vocabular where termen='"
                                        + st.realEscapeString(word)
                                        + "' AND explicatie ='"
                                        + st.realEscapeString(explanation)
                                        + "'";
                                mDbHelper.deleteData(sql);

                                /*
                                 * Check if this category idSectionTemp has
                                 * records left:
                                 */
                                Cursor cur = mDbHelper
                                        .queryData("SELECT COUNT(*) FROM vocabular WHERE idSectiune = '"
                                                + idSectionTemp + "'");
                                int recordsLeft = cur.getInt(0);
                                if (recordsLeft > 0) {
                                    updateSpinner();
                                    createList(curCategoryName);
                                } else {
                                    deleteCategory(idSectionTemp);
                                } // end if there are no records left.
                                // Play a sound for this action:
                                SoundPlayer.playSimple(mFinalContext,
                                        "vocabulary_deleted");
                            } // end if yes button was pressed.
                        }).setNegativeButton(R.string.no, null).show();
    } // end deleteRecord() method.

    /*
     * A method to delete an entire category after last word deletion: the
     * boolean parameter means from menu or not, it must show the alert or not
     * about the section deletion because no records left.
     */
    private void deleteCategory(int idSectionTemp) {
        String sql = "";
        String categoryName = "";

        // Get first the category name if isn't from menu:
        sql = "SELECT nume FROM sectiuni WHERE id='" + idSectionTemp + "'";
        Cursor cur = mDbHelper.queryData(sql);
        categoryName = cur.getString(0);

        // Delete now it:
        sql = "DELETE FROM sectiuni WHERE id='" + idSectionTemp + "'";
        mDbHelper.deleteData(sql);
        mDbHelper.executeSQLCode("VACUUM;");

        // Show an alert and restart the activity:
        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);
        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        // The title:
        String title = getString(R.string.title_category_deleted);
        alert.setTitle(title);

        // The body:
        String body = String.format(getString(R.string.body_category_deleted),
                categoryName);
        alert.setMessage(MyHtml.fromHtml(body));

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (curCategoryName.equals("%") && getNumberOfRecords() > 0) {
                    // Like after a word deletion if all records was
                    // chosen:
                    updateSpinner();
                    createList(curCategoryName);
                } else {
                    recreateThisActivity();
                } // end if we are on all words category.
            }
        });
        alert.show();
    } // end deleteCategory() method.

    // The method which deletes category from menu:
    private void deleteEntireCategory() {
        // First we check if a category is selected:
        if (!curCategoryName.equals("%")) {
            // Get the strings to make an alert:
            String tempTitle = getString(R.string.title_delete_entire_category);
            String tempBody = String.format(
                    getString(R.string.body_delete_entire_category),
                    curCategoryName);

            Context context = new ContextThemeWrapper(this,
                    R.style.MyAlertDialog);
            new AlertDialog.Builder(context)
                    .setTitle(tempTitle)
                    .setMessage(MyHtml.fromHtml(tempBody))
                    .setIcon(android.R.drawable.ic_delete)
                    .setPositiveButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                    // Delete now the category entirely:
                                    /*
                                     * We delete only the section, the records
                                     * will be deleted in cascade:
                                     */
                                    String sql = "DELETE FROM sectiuni WHERE nume='"
                                            + st.realEscapeString(curCategoryName)
                                            + "'";
                                    mDbHelper.deleteData(sql);
                                    mDbHelper.executeSQLCode("VACUUM;");
                                    SoundPlayer.playSimple(mFinalContext,
                                            "vocabulary_deleted");
                                    recreateThisActivity();
                                } // end if yes button was pressed.
                            }).setNegativeButton(R.string.no, null).show();
        } else {
            // If no category is selected, all or nothing:
            GUITools.alert(this, getString(R.string.warning),
                    getString(R.string.warning_no_category_chosen));
        }
    } // end deleteEntireCategory() method.

    // // A method to delete entire vocabulary:
    private void deleteEntireVocabulary() {
        // Get the strings to make an alert:
        String tempTitle = getString(R.string.title_delete_entire_vocabulary);
        String tempBody = getString(R.string.body_delete_entire_vocabulary);

        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);
        new AlertDialog.Builder(context)
                .setTitle(tempTitle)
                .setMessage(MyHtml.fromHtml(tempBody))
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                                // Delete now the vocabulary entirely:
                                /*
                                 * We delete only the sections, the records will
                                 * be deleted in cascade:
                                 */
                                String sql = "DELETE FROM sectiuni";
                                mDbHelper.deleteData(sql);
                                SoundPlayer.playSimple(mFinalContext,
                                        "vocabulary_deleted");
                                recreateThisActivity();
                            } // end if yes button was pressed.
                        }).setNegativeButton(R.string.no, null).show();
    } // end deleteEntireVocabulary() method.

    private void showVocabularyProperties(String word, String explanation,
                                          int idSection, int direction, int curTime) {

        // Make a context:
        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);

        // Create a LinearLayout with ScrollView with all contents as TextViews:
        ScrollView sv = new ScrollView(context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        // TV for word:
        TextView tv = createTextViewForProperties();
        CharSequence text = MyHtml.fromHtml(String.format(
                getString(R.string.properties_word), word));
        tv.setText(text);

        ll.addView(tv);

        // For explanation:
        tv = createTextViewForProperties();
        text = MyHtml.fromHtml(String.format(
                getString(R.string.properties_explanation), explanation));
        tv.setText(text);
        ll.addView(tv);

        // Determine now the section:
        String dictionary = "";
        if (direction == 0) {
            dictionary = getString(R.string.properties_english_romanian);
        } else {
            dictionary = getString(R.string.properties_romanian_english);
        }
        tv = createTextViewForProperties();
        text = MyHtml.fromHtml(String.format(
                getString(R.string.properties_direction), dictionary));
        tv.setText(text);
        ll.addView(tv);

        // For section:
        String sql = "SELECT nume FROM sectiuni WHERE id='" + idSection + "'";
        Cursor cur = mDbHelper.queryData(sql);
        String category = cur.getString(0);
        cur.close();
        tv = createTextViewForProperties();
        text = MyHtml.fromHtml(String.format(
                getString(R.string.properties_category), category));
        tv.setText(text);
        ll.addView(tv);

        // For date:
        String date = GUITools.timeStampToString(this, curTime);
        tv = createTextViewForProperties();
        text = MyHtml.fromHtml(String.format(
                getString(R.string.properties_date), date));
        tv.setText(text);
        ll.addView(tv);

        // Add now the LinearLayout into ScrollView:
        sv.addView(ll);

        // Create now the alert:
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(getString(R.string.alert_properties_title));
        alertDialog.setView(sv);
        alertDialog.setPositiveButton(getString(R.string.bt_close), null);
        AlertDialog alert = alertDialog.create();
        alert.show();
    } // end showVocabularyProperties() method.

    // A method which shows section information:
    private void showSectionInformation() {
        if (!curCategoryName.equals("%")) {

            // Make a new context:
            Context context = new ContextThemeWrapper(this,
                    R.style.MyAlertDialog);

            // Get from database the category row:
            String sql = "SELECT * FROM sectiuni WHERE nume='"
                    + st.realEscapeString(curCategoryName) + "'";
            Cursor cur = mDbHelper.queryData(sql);
            int tempIdSection = cur.getInt(0);
            String tempCategoryName = cur.getString(1);
            int curTime = cur.getInt(3);
            cur.close();

            // Create a LinearLayout with ScrollView with all contents as
            // TextViews:
            ScrollView sv = new ScrollView(context);
            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);

            // For section name:
            TextView tv = createTextViewForProperties();
            CharSequence text = MyHtml.fromHtml(String.format(
                    getString(R.string.info_category_name), tempCategoryName));
            tv.setText(text);
            ll.addView(tv);

            // For contained records:
            cur = mDbHelper
                    .queryData("SELECT COUNT(*) FROM vocabular WHERE idSectiune = '"
                            + tempIdSection + "'");
            int nrRecords = cur.getInt(0);
            cur.close();

            tv = createTextViewForProperties();
            text = MyHtml.fromHtml(String.format(
                    getString(R.string.info_section_contains), "" + nrRecords));
            tv.setText(text);
            ll.addView(tv);

            // For date:
            String date = GUITools.timeStampToString(this, curTime);
            tv = createTextViewForProperties();
            text = MyHtml.fromHtml(String.format(getString(R.string.info_date),
                    date));
            tv.setText(text);
            ll.addView(tv);

            // Add now the LinearLayout into ScrollView:
            sv.addView(ll);

            // Create now the alert:
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog
                    .setTitle(getString(R.string.title_alert_section_information));
            alertDialog.setView(sv);
            alertDialog.setPositiveButton(getString(R.string.bt_close), null);
            AlertDialog alert = alertDialog.create();
            alert.show();

        } else {
            // If no category is selected, all or nothing:
            GUITools.alert(this, getString(R.string.warning),
                    getString(R.string.warning_no_category_chosen));
        }
    } // end showSectionInformation() method.

    // A method to create text view for properties:
    private TextView createTextViewForProperties() {
        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);
        TextView tv = new TextView(context);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
        tv.setPadding(MainActivity.mPaddingDP, MainActivity.mPaddingDP,
                MainActivity.mPaddingDP, MainActivity.mPaddingDP);
        tv.setFocusable(true);
        return tv;
    } // end createTextViewForProperties()() method.

    // A method to create an EditText:
    private EditText createEditText() {
        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);
        EditText et = new EditText(context);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
        et.setPadding(MainActivity.mPaddingDP, MainActivity.mPaddingDP,
                MainActivity.mPaddingDP, MainActivity.mPaddingDP);
        return et;
    } // end createEditText()() method.

    @Override
    public void onDestroy() {
        mDbHelper.close();
        // Shut down also the TTS:
        speak.close();
        super.onDestroy();
    } // end onDestroy method.

    // A method to restart activity:
    public void restartThisActivity(View view) {
        recreateThisActivity();
    } // end restartThisActivity() method.

    // A method which recreates this activity:
    private void recreateThisActivity() {
        startActivity(getIntent());
        finish();
    } // end recreateThisActivity() method.

    @Override
    public void onBackPressed() {
        this.finish();
        GUITools.goToDictionary(this);
    } // end onBackPressed()

    public void onPause() {
        speak.stop();
        super.onPause();
    } // end onPause() method.

    // A method to get numberOfRecords in DB:
    private int getNumberOfRecords() {
        // The number of records in DB:
        String sql = "SELECT COUNT(*) AS total FROM vocabular";
        Cursor cur = mDbHelper.queryData(sql);
        int nr = cur.getInt(0);
        cur.close();
        return nr;
    } // end getNumberOfRecords() method.

    // A method to choose a vocabulary category from assets:
    private void importPredefinedCategories(String dirFrom) {
        int mPaddingDP = MainActivity.mPaddingDP;
        int textSize = MainActivity.textSize;
        Resources res = getResources();

        // Make a new context:
        Context context = new ContextThemeWrapper(this, R.style.MyAlertDialog);

        final AssetManager am = res.getAssets();
        String fileList[];
        try {
            // Create a LinearLayout in ScrollView:
            ScrollView sv = new ScrollView(context);
            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);

            TextView tv = new TextView(context);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            tv.setPadding(0, mPaddingDP, 0, mPaddingDP);
            tv.setText(getString(R.string.body_import_predefined));
            tv.setFocusable(true);
            ll.addView(tv);

            fileList = am.list(dirFrom);
            if (fileList != null) {
                for (int i = 0; i < fileList.length; i++) {
                    final String fileName = fileList[i];
                    String fileNameToShow = fileName.replaceFirst("[.][^.]+$",
                            "");
                    Button bt = new Button(context);
                    bt.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                    bt.setText(fileNameToShow);
                    bt.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertToShow.dismiss();
                            String toPath = getFilesDir().getPath() + "/"
                                    + fileName;
                            if (copyAsset(am, "predefined_categories/"
                                    + fileName, toPath)) {
                                file = new File(toPath);
                                // importFromFile(file);
                            }
                        }
                    });
                    // End add listener for tap the button.
                    ll.addView(bt);
                } // end for.
            } // end if not null the list.
            // Add now the LinearLayout into ScrollView:
            sv.addView(ll);

            // Create the alert:
            AlertDialog.Builder alert = new AlertDialog.Builder(context)
                    .setTitle(getString(R.string.title_import_predefined))
                    .setView(sv)
                    .setNegativeButton(android.R.string.cancel, null);
            alertToShow = alert.create();
            alertToShow.show();
            // end try.
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // end importPredefinedCategories() method.

    // A method to copy a file from AssetsManager:
    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            return false;
        }
    } // end copy from assets to data.

    // A method to copy a file from to:
    private static void copyFile(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    } // end copyFile() method.

    // A method for import predefined from XML button:
    public void importPredefinedCategoriesButton(View view) {
        importPredefinedCategories("predefined_categories");
    } // end importPredefinedCategoriesButton() method.

    /*
     * A method to go into dictionary to search current word: This method is
     * copied from SearchHistoryActivity, if changed there, it must be changed
     * also here.
     */
    public void goToDictionaryAndSearch(String word, int direction) {
        // The word must be processed a little:
        // Remove the "a" and "an":
        if (word.startsWith("a ")) {
            word = word.substring(2);
        } else if (word.startsWith("an ")) {
            word = word.substring(3);
        } // end if word starts with "an".

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("wordFromHistory", word + direction);
        startActivity(intent);
        this.finish();
    } // end goToDictionary and search a word.

    /*
     * Some small methods with VIEW at parameter for the simulated action bar in
     * TV layout. This is necessary because we need to click them from layout
     * resource.
     */
    public void goToDictionary(View view) {
        this.finish();
        GUITools.goToDictionary(this);
    } // end goToDictionaryMethod.

    public void goToIrregularVerbs(View view) {
        this.finish();
        GUITools.goToIrregularVerbs(this);
    } // end goToIrregularVerbs() method.

    public void goToAddRecord(View view) {
        addRecord();
    }

    public void goToShowSectionInformation(View view) {
        showSectionInformation();
    }

    public void goToRenameCategory(View view) {
        renameCategory();
    }

    public void goToImportPredefinedCategories(View view) {
        importPredefinedCategories("predefined_categories");
    }

    public void goToDeleteEntireCategory(View view) {
        deleteEntireCategory();
    }

    public void goToDeleteEntireVocabulary(View view) {
        deleteEntireVocabulary();
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

} // end VocabularyActivity class.
