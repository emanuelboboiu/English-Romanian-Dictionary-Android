package ro.pontes.englishromaniandictionary;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class LexicalResources {

	final Context context;
	private String datamuseWord = "";

	// A variable to know what external resource we need:
	public static int externalResourceType = 1; // IPA by default.

	public LexicalResources(Context context) {
		this.context = context;
	} // end constructor.

	// A general method for external resources from Datamuse:
	public void getExternalResource(int type, String word, String explanation) {
		/*
		 * Insert stats for called Datamuse, from 48 to 58 inclusive, the ID of
		 * the statistic is 48 + type. An array in PHP with all the values is:
		 * $datamuseTypes = new array("phonetic transcription", "synonyms",
		 * "antonyms", "omophones", "rhymes", "word definitions",
		 * "word frequency", "hypernyms", "hyponyms", "frequent followers", "");
		 */
		int statID = 47 + type;
		Statistics stats = new Statistics(context);
		stats.postStats("" + statID, 1);

		/*
		 * Next things will continue for some information only if it is premium
		 * version. frequent followers or frequent predecessors are available
		 * only in premium version. These three information are 10 and 11 as
		 * type.
		 */
		if (MainActivity.isPremium || type < 10) {
			/*
			 * We must determine datamuseWord to be a whole word in the result.
			 * For instance if we have as result "It is a mouse" and the search
			 * is "mou", the word "mouse to be that good determined.
			 */
			// We split the word into an array by spaces:
			String[] words = word.split("\\ ");
			if (words.length > 1) {
				// It means there were spaces:
				for (int i = 0; i < words.length; i++) {
					// Check if word is contained into words[i]:
					if (words[i].contains(MainActivity.lastStringInSearchEdit)) {
						word = words[i];
						datamuseWord = word;
					}
				} // end for.

				/*
				 * Now we check if user searched using spaces, it means string
				 * contains method cannot resolve anything. We make datamuseWord
				 * to be word, word was not changed:
				 */
				if (MainActivity.lastStringInSearchEdit.contains(" ")) {
					datamuseWord = word;
				} // end if user searched using spaces.

			} else {
				// No spaces were found in word:
				datamuseWord = word;
			} // end if no spaces were found.
				// end determine the word to be sent to Datamuse.

			externalResourceType = type;

			// This will happen only if is English - Romanian direction:
			if (MainActivity.direction == 0) {
				// Only if there is a connection to the Internet:
				if (GUITools.isNetworkAvailable(context)) {
					// 1 means IPA phonetic transcription, 2 synonyms, 3
					// antonyms...
					// etc:
					switch (type) {
					case 1: // IPA transcription:
						searchDatamuse(word, 1);
						return;
					case 2: // synonyms:
						searchDatamuse(word, 2);
						return;
					case 3: // antonyms:
						searchDatamuse(word, 3);
						return;
					case 4: // homophones:
						searchDatamuse(word, 4);
						return;
					case 5: // rhymes:
						searchDatamuse(word, 5);
						return;
					case 6: // word definition:
						searchDatamuse(word, 6);
						return;
					case 7: // word frequency:
						searchDatamuse(word, 7);
						return;
					case 8: // hypernyms:
						searchDatamuse(word, 8);
						return;
					case 9: // hyponyms:
						searchDatamuse(word, 9);
						return;
					case 10: // followers:
						searchDatamuse(word, 10);
						return;
					case 11: // predecessors:
						searchDatamuse(word, 11);
						return;
					} // end switch(type).
				} // end if there is connection to Internet.
				else {
					GUITools.alert(
							context,
							context.getString(R.string.warning),
							context.getString(R.string.no_connection_for_external_resource));
				} // end if there is no connection to the Internet.
			} // end if is English - Romanian direction.
			else {
				// A warning message, not English - Romanian direction:
				GUITools.alert(
						context,
						context.getString(R.string.warning),
						context.getString(R.string.information_available_only_in_en_ro));
			} // end if is not English - Romanian direction.
		} // end if is premium or type<56.
		else {
			/*
			 * It is not premium version and user searched for rhymes, frequent
			 * followers or frequent predecessors>
			 */
			// Format the HTML string including the price:
			String premiumMessage = String.format(context
					.getString(R.string.information_available_only_in_premium),
					MainActivity.mUpgradePrice);
			GUITools.alertHTML(context, context.getString(R.string.warning),
					premiumMessage, context.getString(R.string.msg_ok));
		} // end not premium version for some information.
	} // end get externalResource() method.

	// The method for format URLs for search in Datamuse:
	private void searchDatamuse(String word, int type) {
		// We make the URL depending of the type of resource we need:
		String url = "http://api.datamuse.com/words?";
		if (type == 1) { // IPA transcription:
			url += "sp=" + word + "&md=r&ipa=1&max=1";
		} else if (type == 2) { // synonyms:
			url += "rel_syn=" + word + "&max=10";
		} else if (type == 3) { // antonyms:
			url += "rel_ant=" + word + "&max=10";
		} else if (type == 4) { // homophones:
			url += "rel_hom=" + word + "&max=10";
		} else if (type == 5) { // rhymes:
			url += "rel_rhy=" + word + "&max=30";
		} else if (type == 6) { // definition:
			url += "sp=" + word + "&md=d&max=1";
		} else if (type == 7) { // word frequency:
			url += "sp=" + word + "&md=f&max=1";
		} else if (type == 8) { // hypernyms:
			url += "rel_spc=" + word + "&max=10";
		} else if (type == 9) { // hyponyms:
			url += "rel_gen=" + word + "&max=30";
		} else if (type == 10) { // followers:
			url += "rel_bga=" + word + "&max=30";
		} else if (type == 11) { // predecessors:
			url += "rel_bgb=" + word + "&max=30";
		}

		// Add now the Datamuse key to identify my application there:
		url += "&k=boboiu";

		new GetDatamuse().execute(url);
	} // end ipaResult() method.

	/*
	 * Here is a subclass to take and parse JSon for other resources for the
	 * dictionary.
	 */
	private class GetDatamuse extends AsyncTask<String, String, String> {
		@SuppressWarnings("deprecation")
		private ProgressDialog pd;

		// execute before task:
		@SuppressWarnings("deprecation")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setMessage(context
					.getString(R.string.please_wait_external_resources));
			pd.setIndeterminate(false);
			pd.setCancelable(true);
			pd.show();
		}

		// Execute task
		String urlText = "";

		@Override
		protected String doInBackground(String... strings) {
			StringBuilder content = new StringBuilder();
			urlText = strings[0];
			try {
				// Create a URL object:
				URL url = new URL(urlText);
				// Create a URLConnection object:
				URLConnection urlConnection = url.openConnection();
				// Wrap the URLConnection in a BufferedReader:
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(urlConnection.getInputStream()));
				String line;
				// Read from the URLConnection via the BufferedReader:
				while ((line = bufferedReader.readLine()) != null) {
					content.append(line);
				}
				bufferedReader.close();
			} catch (Exception e) {
				// e.printStackTrace();
			}
			return content.toString();
		} // end doInBackground() method.

		// Execute after task with the task result as string:
		@Override
		protected void onPostExecute(String s) {

			// Clear progress dialog:
			pd.dismiss();

			// we make things depending of the externalResourceType:
			// Before, a specific sound:
			SoundPlayer.playSimple(context, "new_dialog");

			switch (externalResourceType) {
			case 1:
				// IPA transcription:
				showIPATranscription(s);
				return;
				// end case IPA.
			case 2:
				// Synonyms list:
				showRelList(s, 2);
				return;
				// end case synonyms.
			case 3:
				// Antonyms list:
				showRelList(s, 3);
				return;
				// end case antonyms.
			case 4:
				// Homophones list:
				showRelList(s, 4);
				return;
				// end case homophones.
			case 5:
				// Rhymes list:
				showRelList(s, 5);
				return;
				// end case rhymes.
			case 6:
				// Word definition:
				showWordDefinition(s);
				return;
				// end case word definition.
			case 7:
				// Word frequency:
				showWordFrequency(s);
				return;
				// end case word frequency.
			case 8:
				// Hypernyms list:
				showRelList(s, 8);
				return;
				// end case hypernyms.
			case 9:
				// Hyponyms list:
				showRelList(s, 9);
				return;
				// end case hyponyms.
			case 10:
				// Followers list:
				showRelList(s, 10);
				return;
				// end case followers.
			case 11:
				// Predecessors list:
				showRelList(s, 11);
				return;
				// end case predecessors.
			} // end switch(externalResourceType).
		} // end postExecute() method.
	} // end subclass.

	// The methods called from postExecute:
	public void showIPATranscription(String s) {
		boolean isError = false;

		if (s.length() < 10) {
			// The string passed is too short, no a good result from JSON:
			isError = true;
		}

		String word = "", ipaPron = "", arpabetPron = "";

		// Do something with the interface:
		// Parse JSON data:
		try {
			JSONArray jArray = new JSONArray(s);
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject jObject = jArray.getJSONObject(i);
				word = jObject.getString("word");

				// This will be another JSON object:
				JSONArray arr2 = jObject.getJSONArray("tags");
				ipaPron = arr2.getString(1);
				arpabetPron = arr2.getString(0);
			} // end loop.
		} catch (JSONException e) {
			// Do nothing yet.
			isError = true;
		} // end catch (JSONException e)
			// end parse JSON data.

		/*
		 * We need to split the array for IPA by colon, the second index is the
		 * effective IPA transcription:
		 */
		String[] ipa = ipaPron.split("\\:");

		/*
		 * We need to split the array for Arpabet by colon, the second index is
		 * the effective Arpabet transcription:
		 */
		String[] arpabet = arpabetPron.split("\\:");

		// Only if the arrays have at least 2 in length:
		if (ipa.length >= 2 && arpabet.length >= 2 && !isError) {
			// Here we can process a little the two results:
			String ipaShow = ipa[1];
			String arpabetShow = arpabet[1];
			// We cut the last space from arpabet transcription:
			if (arpabetShow.endsWith(" ")) {
				arpabetShow = arpabetShow
						.substring(0, arpabetShow.length() - 1);
			} // end if arpabet ends in space.
			String ipaMessage = String.format(
					context.getString(R.string.ipa_transcription_message),
					word, ipaShow, arpabetShow);
			GUITools.alertHTML(context,
					context.getString(R.string.ipa_transcription_title),
					ipaMessage, context.getString(R.string.bt_close));
		} // end if there are no errors.
		else {
			GUITools.alert(context, context.getString(R.string.warning),
					context.getString(R.string.information_not_available));
		}
	} // end showIPATranscription() method.

	// The method to show word definition:
	public void showWordDefinition(String s) {
		boolean isError = false;

		if (s.length() < 10) {
			// The string passed is too short, no a good result from JSON:
			isError = true;
		}

		String word = "", definition = "", definitions = "";

		// Do something with the interface:
		// Parse JSON data:
		try {
			JSONArray jArray = new JSONArray(s);
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject jObject = jArray.getJSONObject(i);
				word = jObject.getString("word");

				// This will be another JSON object:
				JSONArray arr2 = jObject.getJSONArray("defs");
				for (int j = 0; j < arr2.length(); j++) {
					definition = arr2.getString(j);
					definitions += (j + 1) + ". "
							+ definition.replace("\t", " - ") + "&lt;br>";
				} // end inner loop.
			} // end loop.
		} catch (JSONException e) {
			isError = true;
		} // end catch (JSONException e)
			// end parse JSON data.

		if (!isError) {
			CharSequence cs = MyHtml.fromHtml(definitions);
			String defMessage = String.format(
					context.getString(R.string.definition_message), word, cs);
			GUITools.alertHTML(context,
					context.getString(R.string.definition_title), defMessage,
					context.getString(R.string.bt_close));
		} // end if there are no errors.
		else {
			GUITools.alert(context, context.getString(R.string.warning),
					context.getString(R.string.information_not_available));
		}
	} // end showWordDefinition() method.

	// Show word frequency per million:
	public void showWordFrequency(String s) {
		boolean isError = false;

		if (s.length() < 10) {
			// The string passed is too short, no a good result from JSON:
			isError = true;
		}

		String word = "", frequency = "";

		// Do something with the interface:
		// Parse JSON data:
		try {
			JSONArray jArray = new JSONArray(s);
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject jObject = jArray.getJSONObject(i);
				word = jObject.getString("word");

				// This will be another JSON object:
				JSONArray arr2 = jObject.getJSONArray("tags");
				frequency = arr2.getString(0);
			} // end loop.
		} catch (JSONException e) {
			// Do nothing yet.
			isError = true;
		} // end catch (JSONException e)
			// end parse JSON data.

		/*
		 * We need to split the array for IPA by colon, the second index is the
		 * effective frequency:
		 */
		String[] arrFrequency = frequency.split("\\:");

		// Only if the array has at least 2 in length:
		if (arrFrequency.length >= 2 && !isError) {
			// Convert the frequency number to 2 decimals:
			double temp = Double.parseDouble(arrFrequency[1]);
			temp = GUITools.round(temp, 2);
			arrFrequency[1] = "" + temp;
			String frequencyMessage = String.format(
					context.getString(R.string.word_frequency_message), word,
					arrFrequency[1]);
			GUITools.alertHTML(context,
					context.getString(R.string.word_frequency_title),
					frequencyMessage, context.getString(R.string.bt_close));
		} // end if there are no errors.
		else {
			GUITools.alert(context, context.getString(R.string.warning),
					context.getString(R.string.information_not_available));
		}
	} // end showWordFrequency() method.

	// The method to parse JSON and show vocabulary lists:
	private void showRelList(String s, int relType) {
		boolean isError = false;
		if (s.length() < 10) {
			// The string passed is too short, no a good result from JSON:
			isError = true;
		}

		StringBuilder sb = new StringBuilder();

		// Do something with the interface:
		// Parse JSON data:
		try {
			JSONArray jArray = new JSONArray(s);
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject jObject = jArray.getJSONObject(i);
				String curWord = jObject.getString("word");
				sb.append(curWord + ", ");
			} // end loop.
		} catch (JSONException e) {
			isError = true;
		} // end catch (JSONException e)
			// end parse JSON data.

		// Cut the last comma from the string:
		String relList = sb.toString();
		if (relList.length() > 2) {
			relList = relList.substring(0, relList.length() - 2);
		} // end if there is a minimum length of synList.
		else {
			// The length of relList is 0:
			isError = true;
		}

		if (!isError) {
			/*
			 * Get the title and message strings resource, depending of the
			 * relations type:
			 */
			String relTitle = "";
			String relMessage = "";

			if (relType == 2) { // synonyms:
				relTitle = context.getString(R.string.synonyms_list_title);
				relMessage = context.getString(R.string.synonyms_list_message);
			} else if (relType == 3) { // antonyms:
				relTitle = context.getString(R.string.antonyms_list_title);
				relMessage = context.getString(R.string.antonyms_list_message);
			} else if (relType == 4) { // homophones:
				relTitle = context.getString(R.string.homophones_list_title);
				relMessage = context
						.getString(R.string.homophones_list_message);
			} else if (relType == 5) { // rhymes:
				relTitle = context.getString(R.string.rhymes_list_title);
				relMessage = context.getString(R.string.rhymes_list_message);
			} else if (relType == 8) { // hypernyms:
				relTitle = context.getString(R.string.hypernyms_list_title);
				relMessage = context.getString(R.string.hypernyms_list_message);
			} else if (relType == 9) { // hyponyms:
				relTitle = context.getString(R.string.hyponyms_list_title);
				relMessage = context.getString(R.string.hyponyms_list_message);
			} else if (relType == 10) { // followers:
				relTitle = context.getString(R.string.followers_list_title);
				relMessage = context.getString(R.string.followers_list_message);
			} else if (relType == 11) { // predecessors:
				relTitle = context.getString(R.string.predecessors_list_title);
				relMessage = context
						.getString(R.string.predecessors_list_message);
			}

			String relMessageFormated = String.format(relMessage, datamuseWord,
					relList);
			GUITools.alertHTML(context, relTitle, relMessageFormated,
					context.getString(R.string.bt_close));
		} // end if is not an error..
		else {
			GUITools.alert(context, context.getString(R.string.warning),
					context.getString(R.string.information_not_available));
		}
	} // end showRelList() method.;

} // end LexicalResources class.
