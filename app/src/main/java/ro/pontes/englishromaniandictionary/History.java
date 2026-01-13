package ro.pontes.englishromaniandictionary;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class History {

    // An ArrayList with strings:
    private final ArrayList<String> mHistory = new ArrayList<>();

    // We need also a context at least for creating TextViews:
    private final Context context;

    // We need a space around text views:
    private final int mPaddingDP;

    // The constructor:
    public History(Context context) {
        this.context = context;

        // Calculate the pixels in DP for mPaddingDP, for TextViews.
        int paddingPixel = 3;
        float density = context.getResources().getDisplayMetrics().density;
        mPaddingDP = (int) (paddingPixel * density);
        // end calculate mPaddingDP
    } // end constructor.

    public void add(String message) {
        mHistory.add(message);
    } // end add() method.

    // A method to add as first item:
    public void addAsFirst(String message) {
        mHistory.add(0, message);
    } // end addAsFirst() method.

    // A method to clear the history:
    public void clear() {
        mHistory.clear();
    } // end clear() method.

    // A method to return history in an alert LinearLayout:
    public ScrollView getSVHistory() {
        // Get the textSize of the text in TextViews:
        int textSize = MainActivity.textSize;

        // Create a LinearLayout with ScrollView with all contents as TextViews:
        ScrollView sv = new ScrollView(context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        // A for for each message in the history as TextView:
        for (int i = 0; i < mHistory.size(); i++) {
            TextView tv = new TextView(context);
            // If is first item, it's title:
            if (i == 0) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize + 2);
                tv.setPadding(mPaddingDP, mPaddingDP * 3, mPaddingDP, mPaddingDP * 2);
                tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            } else {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
            }
            tv.setText(mHistory.get(i));
            tv.setFocusable(true);
            ll.addView(tv);
        } // end for.

        sv.addView(ll);

        return sv;
    } // end getSVHistory() method.

} // end History class.
