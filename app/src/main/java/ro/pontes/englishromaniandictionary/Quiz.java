package ro.pontes.englishromaniandictionary;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * Started by Manu on 18 September 2015, 00:06
 */

public class Quiz {

    protected Context context;
    protected DBAdapter mDbHelper;
    protected LinearLayout llTop;
    protected LinearLayout llMain;
    protected static int mPaddingDP = MainActivity.mPaddingDP;
    protected final int paddingMultiplier = 10;
    protected static int textSize = MainActivity.textSize;
    public Button btList;
    public Button btQuiz;
    protected boolean isStarted = false;
    protected SpeakText speak;
    protected StringTools st;

    // The constructor:
    public Quiz(Context context, LinearLayout llMainPart) {
        this.context = context;

        // Clear the llMainPart LinearLayout:
        llMainPart.removeAllViews();

        // Create the llTop:
        LinearLayout llTop = new LinearLayout(context);
        llTop.setOrientation(LinearLayout.VERTICAL);
        llTop.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams llTopParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        // Add it into llMainPart:
        llMainPart.addView(llTop, llTopParams);
        this.llTop = llTop;

        // Create the llMain:
        LinearLayout llMain = new LinearLayout(context);
        llMain.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams llMainParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        // Add it into llMainPart:
        llMainPart.addView(llMain, llMainParams);
        this.llMain = llMain;

        // Start things for our database:
        mDbHelper = new DBAdapter(this.context);
        mDbHelper.createDatabase();
        mDbHelper.open();
        // end start things for our database.

        speak = new SpeakText(this.context);
        st = new StringTools(this.context);
    } // end constructor.

    // A method which clear all the layout for quiz:
    protected void clearLL() {
        llTop.removeAllViews();
        llMain.removeAllViews();
    } // end clearLL() method.

    // Create a text view with a text on it:
    protected TextView createTextView(String text, int textSize) {
        TextView tv = new TextView(context);
        // Polish this text view object:
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
        tv.setText(text);
        tv.setFocusable(true);
        return tv;
    } // end createTextView() method.

    // Create a button with a text on it:
    protected Button createButton(String text, int textSize) {
        Button bt = new Button(context);
        // Polish this Button object:
        bt.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        bt.setGravity(Gravity.CENTER_HORIZONTAL);
        bt.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
        bt.setText(text);
        return bt;
    } // end createButton() method.

    // Create a check box with a text on it:
    protected CheckBox createCheckBox(String text, int textSize) {
        CheckBox cbt = new CheckBox(context);
        // Polish this CheckBox object:
        cbt.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        cbt.setGravity(Gravity.CENTER_HORIZONTAL);
        cbt.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
        cbt.setText(text);
        return cbt;
    } // end createCheckBox() method.

    // A method to enable or disable buttons:
    protected void enableOrDisableButtons() {
        if (isStarted) {
            btList.setEnabled(false);
            btQuiz.setEnabled(false);
        } else {
            btList.setEnabled(true);
            btQuiz.setEnabled(true);
        }
    } // end enableOrDisableButtons.

} // end Quiz class.
