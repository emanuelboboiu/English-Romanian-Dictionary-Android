package ro.pontes.englishromaniandictionary;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class History {

	// An ArrayList with strings:
	private ArrayList<String> mHistory = new ArrayList<String>();

	// We need also a context at least for creating TextViews:
	private Context context;

	// We need a space around text views:
	private int mPaddingDP = 3;

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
				tv.setPadding(mPaddingDP, mPaddingDP * 3, mPaddingDP,
						mPaddingDP * 2);
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

	// Create a button with a text on it:
	protected Button createButton(String text, int textSize) {
		Button bt = new Button(context);
		// Polish this Button object:
		bt.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
		bt.setGravity(Gravity.CENTER_HORIZONTAL);
		bt.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
		bt.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		bt.setText(text);
		return bt;
	} // end createButton() method.

} // end History class.
