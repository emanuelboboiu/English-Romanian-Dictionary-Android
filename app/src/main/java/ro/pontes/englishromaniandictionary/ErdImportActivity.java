package ro.pontes.englishromaniandictionary;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Locale;

/** Isolated external entry point. Works without MainActivity or its static state. */
public class ErdImportActivity extends ComponentActivity {
    private VocabularyTransfer transfer;

    @Override protected void attachBaseContext(Context base) {
        int language = new Settings(base).getIntSettings("langNumber");
        if (language == 1 || language == 2) {
            Configuration config = new Configuration(base.getResources().getConfiguration());
            config.setLocale(new Locale(language == 1 ? "en" : "ro"));
            base = base.createConfigurationContext(config);
        }
        super.attachBaseContext(base);
    }

    static boolean accepts(Intent intent) {
        return intent != null && Intent.ACTION_VIEW.equals(intent.getAction())
                && intent.getData() != null && "content".equals(intent.getData().getScheme());
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        int padding = Math.round(16 * getResources().getDisplayMetrics().density);
        ViewCompat.setOnApplyWindowInsetsListener(scroll, (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(padding + bars.left, padding + bars.top,
                    padding + bars.right, padding + bars.bottom);
            return insets;
        });
        scroll.addView(body);
        setContentView(scroll);
        TextView info = new TextView(this);
        info.setText(R.string.erd_external_info);
        info.setTextSize(20);
        body.addView(info);
        Button dictionary = new Button(this);
        dictionary.setText(R.string.mnu_go_to_dictionary);
        dictionary.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        body.addView(dictionary);
        Button close = new Button(this);
        close.setText(R.string.msg_close);
        close.setOnClickListener(view -> finish());
        body.addView(close);
        close.requestFocus();
        transfer = new VocabularyTransfer(this, () -> info.setText(R.string.erd_external_finished));
        if (accepts(getIntent())) {
            transfer.openExternal(getIntent().getData());
        } else {
            info.setText(R.string.erd_import_error);
        }
    }

    @Override protected void onDestroy() {
        if (transfer != null) transfer.close();
        super.onDestroy();
    }
}
