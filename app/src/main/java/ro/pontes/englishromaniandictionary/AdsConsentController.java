package ro.pontes.englishromaniandictionary;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

/** One consent gate shared by all banner screens. All view work stays on the UI thread. */
final class AdsConsentController implements DefaultLifecycleObserver, MenuProvider {
    private static int privacyGeneration;
    private final ComponentActivity activity;
    private final AdView banner;
    private final ConsentInformation consent;
    private final int generation = privacyGeneration;
    private boolean started;
    private boolean destroyed;
    private boolean formOpen;

    AdsConsentController(ComponentActivity activity, AdView banner) {
        this.activity = activity;
        this.banner = banner;
        consent = UserMessagingPlatform.getConsentInformation(activity);
        activity.getLifecycle().addObserver(this);
        activity.addMenuProvider(this);
        consent.requestConsentInfoUpdate(activity, new ConsentRequestParameters.Builder().build(),
                () -> {
                    if (!isAlive()) return;
                    activity.invalidateMenu();
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity, error -> {
                        if (error != null) Log.w("AdsConsent", error.getMessage());
                        if (!isAlive()) return;
                        activity.invalidateMenu();
                        requestAd();
                    });
                }, error -> {
                    Log.w("AdsConsent", error.getMessage());
                    if (isAlive()) activity.invalidateMenu();
                    requestAd(); // Only a still-valid previous decision may allow ads offline.
                });
        requestAd();
    }

    private boolean isAlive() {
        return !destroyed && !activity.isFinishing() && !activity.isDestroyed();
    }

    private boolean mayRequestAds() {
        return isAlive() && !formOpen && !MainActivity.isPremium && !MainActivity.isTV
                && generation == privacyGeneration && consent.canRequestAds();
    }

    private void requestAd() {
        if (started || !mayRequestAds()) return;
        started = true;
        MobileAds.initialize(activity.getApplicationContext(), status -> activity.runOnUiThread(() -> {
            if (!mayRequestAds()) return;
            banner.loadAd(new AdRequest.Builder().build());
            if (!activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                banner.pause();
            }
        }));
    }

    @Override public void onResume(@NonNull LifecycleOwner owner) {
        if (generation != privacyGeneration || MainActivity.isPremium) {
            activity.recreate();
        } else if (!destroyed) {
            banner.resume();
        }
    }

    @Override public void onPause(@NonNull LifecycleOwner owner) {
        if (!destroyed) banner.pause();
    }

    @Override public void onDestroy(@NonNull LifecycleOwner owner) {
        destroyBanner();
        activity.removeMenuProvider(this);
    }

    private void destroyBanner() {
        if (!destroyed) {
            destroyed = true;
            banner.destroy();
        }
    }

    @Override public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.add(Menu.NONE, R.id.menu_ad_privacy, 800, R.string.ad_privacy_options)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        onPrepareMenu(menu);
    }

    @Override public void onPrepareMenu(@NonNull Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_ad_privacy);
        if (item != null) item.setVisible(consent.getPrivacyOptionsRequirementStatus()
                == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED);
    }

    @Override public boolean onMenuItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() != R.id.menu_ad_privacy) return false;
        if (formOpen || !isAlive()) return true;
        formOpen = true;
        // Discard the old ad before choices change, including automatic refreshes.
        destroyBanner();
        UserMessagingPlatform.showPrivacyOptionsForm(activity, error -> {
            if (activity.isFinishing() || activity.isDestroyed()) return;
            if (error != null) {
                Log.w("AdsConsent", error.getMessage());
                Toast.makeText(activity, R.string.ad_privacy_error, Toast.LENGTH_LONG).show();
            }
            privacyGeneration++;
            activity.recreate();
        });
        return true;
    }
}
