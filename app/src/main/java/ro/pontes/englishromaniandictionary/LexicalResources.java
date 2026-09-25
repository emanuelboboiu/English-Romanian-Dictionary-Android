package ro.pontes.englishromaniandictionary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;

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
            String[] words = word.split(" ");
            if (words.length > 1) {
                // It means there were spaces:
                for (String s : words) {
                    // Check if word is contained into words[i]:
                    if (s.contains(MainActivity.lastStringInSearchEdit)) {
                        word = s;
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
                    } // end switch(type).
                } // end if there is connection to Internet.
                else {
                    GUITools.alert(context, context.getString(R.string.warning), context.getString(R.string.no_connection_for_external_resource));
                } // end if there is no connection to the Internet.
            } // end if is English - Romanian direction.
            else {
                // A warning message, not English - Romanian direction:
                GUITools.alert(context, context.getString(R.string.warning), context.getString(R.string.information_available_only_in_en_ro));
            } // end if is not English - Romanian direction.
        } // end if is premium or type<56.
        else {
            /*
             * It is not premium version and user searched for rhymes, frequent
             * followers or frequent predecessors>
             */
            // Format the HTML string including the price:
            String premiumMessage = String.format(context.getString(R.string.information_available_only_in_premium), MainActivity.mUpgradePrice);
            GUITools.alertHTML(context, context.getString(R.string.warning), premiumMessage, context.getString(R.string.msg_ok));
        } // end not premium version for some information.
    } // end get externalResource() method.

    // The method for format URLs for search in Datamuse:
    private void searchDatamuse(String word, int type) {
        String baseUrl = "https://api.datamuse.com/words";
        String url;
        if (type == 1) { // IPA transcription:
            url = WebDataClient.buildUrl(baseUrl, "sp", word, "md", "r", "ipa", "1", "max", "1", "k", "boboiu");
        } else if (type == 2) { // synonyms:
            url = WebDataClient.buildUrl(baseUrl, "rel_syn", word, "max", "10", "k", "boboiu");
        } else if (type == 3) { // antonyms:
            url = WebDataClient.buildUrl(baseUrl, "rel_ant", word, "max", "10", "k", "boboiu");
        } else if (type == 4) { // homophones:
            url = WebDataClient.buildUrl(baseUrl, "rel_hom", word, "max", "10", "k", "boboiu");
        } else if (type == 5) { // rhymes:
            url = WebDataClient.buildUrl(baseUrl, "rel_rhy", word, "max", "30", "k", "boboiu");
        } else if (type == 6) { // definition:
            url = WebDataClient.buildUrl(baseUrl, "sp", word, "md", "d", "max", "1", "k", "boboiu");
        } else if (type == 7) { // word frequency:
            url = WebDataClient.buildUrl(baseUrl, "sp", word, "md", "f", "max", "1", "k", "boboiu");
        } else if (type == 8) { // hypernyms:
            url = WebDataClient.buildUrl(baseUrl, "rel_spc", word, "max", "10", "k", "boboiu");
        } else if (type == 9) { // hyponyms:
            url = WebDataClient.buildUrl(baseUrl, "rel_gen", word, "max", "30", "k", "boboiu");
        } else if (type == 10) { // followers:
            url = WebDataClient.buildUrl(baseUrl, "rel_bga", word, "max", "30", "k", "boboiu");
        } else if (type == 11) { // predecessors:
            url = WebDataClient.buildUrl(baseUrl, "rel_bgb", word, "max", "30", "k", "boboiu");
        } else {
            return;
        }

        loadDatamuse(url);
    } // end ipaResult() method.

    private void loadDatamuse(String url) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.please_wait_external_resources));
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        progressDialog.show();

        final int requestedResourceType = externalResourceType;
        WebDataClient.get(url, result -> {
            if (!WebDataClient.isUiContextActive(context)) {
                return;
            }
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            showDatamuseResult(result, requestedResourceType);
        });
    }

    private void showDatamuseResult(String result, int resourceType) {
        SoundPlayer.playSimple(context, "new_dialog");

        switch (resourceType) {
                case 1:
                    // IPA transcription:
                    showIPATranscription(result);
                    return;
                // end case IPA.
                case 2:
                    // Synonyms list:
                    showRelList(result, 2);
                    return;
                // end case synonyms.
                case 3:
                    // Antonyms list:
                    showRelList(result, 3);
                    return;
                // end case antonyms.
                case 4:
                    // Homophones list:
                    showRelList(result, 4);
                    return;
                // end case homophones.
                case 5:
                    // Rhymes list:
                    showRelList(result, 5);
                    return;
                // end case rhymes.
                case 6:
                    // Word definition:
                    showWordDefinition(result);
                    return;
                // end case word definition.
                case 7:
                    // Word frequency:
                    showWordFrequency(result);
                    return;
                // end case word frequency.
                case 8:
                    // Hypernyms list:
                    showRelList(result, 8);
                    return;
                // end case hypernyms.
                case 9:
                    // Hyponyms list:
                    showRelList(result, 9);
                    return;
                // end case hyponyms.
                case 10:
                    // Followers list:
                    showRelList(result, 10);
                    return;
                // end case followers.
                case 11:
                    // Predecessors list:
                    showRelList(result, 11);
                    // end case predecessors.
        }
    }

    // The methods called from postExecute:
    public void showIPATranscription(String s) {
        // The string passed is too short, no a good result from JSON:
        boolean isError = s.length() < 10;
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
        String[] ipa = ipaPron.split(":");

        /*
         * We need to split the array for Arpabet by colon, the second index is
         * the effective Arpabet transcription:
         */
        String[] arpabet = arpabetPron.split(":");

        // Only if the arrays have at least 2 in length:
        if (ipa.length >= 2 && arpabet.length >= 2 && !isError) {
            // Here we can process a little the two results:
            String ipaShow = ipa[1];
            String arpabetShow = arpabet[1];
            // We cut the last space from arpabet transcription:
            if (arpabetShow.endsWith(" ")) {
                arpabetShow = arpabetShow.substring(0, arpabetShow.length() - 1);
            } // end if arpabet ends in space.
            String ipaMessage = String.format(context.getString(R.string.ipa_transcription_message), word, ipaShow, arpabetShow);
            GUITools.alertHTML(context, context.getString(R.string.ipa_transcription_title), ipaMessage, context.getString(R.string.bt_close));
        } // end if there are no errors.
        else {
            GUITools.alert(context, context.getString(R.string.warning), context.getString(R.string.information_not_available));
        }
    } // end showIPATranscription() method.

    // The method to show word definition:
    public void showWordDefinition(String s) {
        boolean isError = s.length() < 10;
        // The string passed is too short, no a good result from JSON:
        String word = "";
        String definition;
        StringBuilder definitions = new StringBuilder();

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
                    definitions.append(j + 1).append(". ").append(definition.replace("\t", " - ")).append("&lt;br>");
                } // end inner loop.
            } // end loop.
        } catch (JSONException e) {
            isError = true;
        } // end catch (JSONException e)
        // end parse JSON data.

        if (!isError) {
            CharSequence cs = MyHtml.fromHtml(definitions.toString());
            String defMessage = String.format(context.getString(R.string.definition_message), word, cs);
            GUITools.alertHTML(context, context.getString(R.string.definition_title), defMessage, context.getString(R.string.bt_close));
        } // end if there are no errors.
        else {
            GUITools.alert(context, context.getString(R.string.warning), context.getString(R.string.information_not_available));
        }
    } // end showWordDefinition() method.

    // Show word frequency per million:
    public void showWordFrequency(String s) {
        // The string passed is too short, no a good result from JSON:
        boolean isError = s.length() < 10;

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
        String[] arrFrequency = frequency.split(":");

        // Only if the array has at least 2 in length:
        if (arrFrequency.length >= 2 && !isError) {
            // Convert the frequency number to 2 decimals:
            double temp = Double.parseDouble(arrFrequency[1]);
            temp = GUITools.round(temp, 2);
            arrFrequency[1] = "" + temp;
            String frequencyMessage = String.format(context.getString(R.string.word_frequency_message), word, arrFrequency[1]);
            GUITools.alertHTML(context, context.getString(R.string.word_frequency_title), frequencyMessage, context.getString(R.string.bt_close));
        } // end if there are no errors.
        else {
            GUITools.alert(context, context.getString(R.string.warning), context.getString(R.string.information_not_available));
        }
    } // end showWordFrequency() method.

    // The method to parse JSON and show vocabulary lists:
    private void showRelList(String s, int relType) {
        // The string passed is too short, no a good result from JSON:
        boolean isError = s.length() < 10;

        StringBuilder sb = new StringBuilder();

        // Do something with the interface:
        // Parse JSON data:
        try {
            JSONArray jArray = new JSONArray(s);
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                String curWord = jObject.getString("word");
                sb.append(curWord).append(", ");
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
                relMessage = context.getString(R.string.homophones_list_message);
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
                relMessage = context.getString(R.string.predecessors_list_message);
            }

            String relMessageFormated = String.format(relMessage, datamuseWord, relList);
            GUITools.alertHTML(context, relTitle, relMessageFormated, context.getString(R.string.bt_close));
        } // end if is not an error..
        else {
            GUITools.alert(context, context.getString(R.string.warning), context.getString(R.string.information_not_available));
        }
    } // end showRelList() method.;

} // end LexicalResources class.
