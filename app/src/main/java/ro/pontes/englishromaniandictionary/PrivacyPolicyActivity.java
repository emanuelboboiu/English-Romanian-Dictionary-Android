package ro.pontes.englishromaniandictionary;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Read-only HTTPS policy viewer; never launches another app on TV. */
public class PrivacyPolicyActivity extends ComponentActivity {
    static final String POLICY_URL = "https://android.pontes.ro/erd/privacy.html";
    private WebView web;
    private TextView status;
    private boolean failed;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int margin = Math.round(16 * getResources().getDisplayMetrics().density);
        root.setPadding(margin, margin, margin, margin);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(margin + bars.left, margin + bars.top,
                    margin + bars.right, margin + bars.bottom);
            return insets;
        });
        setContentView(root);
        LinearLayout controls = new LinearLayout(this);
        root.addView(controls);
        Button up = button(controls, R.string.privacy_page_up);
        Button down = button(controls, R.string.privacy_page_down);
        Button reload = button(controls, R.string.privacy_reload);
        Button close = button(controls, R.string.msg_close);
        status = new TextView(this);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        root.addView(status);
        web = new WebView(this);
        root.addView(web, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        // No scripts, Java bridge, file/content access, or cleartext subresources.
        web.getSettings().setJavaScriptEnabled(false);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return blockNonHttps(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return blockNonHttps(Uri.parse(url)); // Android 6 fallback.
            }
            @Override public void onPageStarted(WebView view, String url, Bitmap icon) {
                failed = false;
                status.setText(R.string.privacy_loading);
                status.setVisibility(View.VISIBLE);
            }
            @Override public void onPageFinished(WebView view, String url) {
                if (!failed) status.setVisibility(View.GONE);
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) showError();
            }
            @Override public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse response) {
                if (request.isForMainFrame()) showError();
            }
            // TLS errors retain WebView's default cancellation; never bypass certificate checks.
        });
        up.setOnClickListener(view -> web.pageUp(false));
        down.setOnClickListener(view -> web.pageDown(false));
        reload.setOnClickListener(view -> web.loadUrl(POLICY_URL));
        close.setOnClickListener(view -> finish());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (web.canGoBack()) web.goBack();
                else finish();
            }
        });
        if (state == null || web.restoreState(state) == null) web.loadUrl(POLICY_URL);
        down.requestFocus();
    }

    private Button button(LinearLayout row, int label) {
        Button button = new Button(this);
        button.setText(label);
        row.addView(button, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return button;
    }

    private boolean blockNonHttps(Uri uri) {
        if ("https".equalsIgnoreCase(uri.getScheme())) return false;
        Toast.makeText(this, getString(R.string.privacy_link_unavailable, uri.toString()), Toast.LENGTH_LONG).show();
        return true;
    }

    private void showError() {
        failed = true;
        status.setText(R.string.privacy_load_error);
        status.setVisibility(View.VISIBLE);
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        web.saveState(state);
        super.onSaveInstanceState(state);
    }

    @Override protected void onPause() {
        web.onPause();
        super.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
        if (web != null) web.onResume();
    }

    @Override protected void onDestroy() {
        web.stopLoading();
        ((ViewGroup) web.getParent()).removeView(web);
        web.destroy();
        super.onDestroy();
    }
}
