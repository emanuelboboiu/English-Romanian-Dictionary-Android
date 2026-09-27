package ro.pontes.englishromaniandictionary;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import static org.junit.Assert.*;

public class ErdAssociationTest {
    private boolean resolves(String uri, String type) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse(uri), type).setPackage(context.getPackageName());
        for (ResolveInfo resolved : context.getPackageManager().queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY)) {
            if (ErdImportActivity.class.getName().equals(resolved.activityInfo.name)) return true;
        }
        return false;
    }

    @Test public void resolvesOpaqueDocumentUrisForCommonTypes() {
        for (String type : new String[]{"application/octet-stream", "text/plain", "application/x-erd"}) {
            assertTrue(resolves("content://example.documents/document/123", type));
        }
    }

    @Test public void doesNotClaimWebImagesOrRawFilePaths() {
        assertFalse(resolves("https://example.com/sample.erd", "application/octet-stream"));
        assertFalse(resolves("file:///data/user/0/private.erd", "application/octet-stream"));
        assertFalse(resolves("content://example.documents/document/123", "image/jpeg"));
    }

    @Test public void rejectsInvalidExplicitIntents() {
        assertFalse(ErdImportActivity.accepts(null));
        assertFalse(ErdImportActivity.accepts(new Intent(Intent.ACTION_VIEW)));
        assertFalse(ErdImportActivity.accepts(new Intent(Intent.ACTION_VIEW, Uri.parse("file:///private.erd"))));
        assertFalse(ErdImportActivity.accepts(new Intent(Intent.ACTION_SEND, Uri.parse("content://example/123"))));
        assertTrue(ErdImportActivity.accepts(new Intent(Intent.ACTION_VIEW, Uri.parse("content://example/123"))));
    }
}
