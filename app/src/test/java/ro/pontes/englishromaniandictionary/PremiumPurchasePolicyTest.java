package ro.pontes.englishromaniandictionary;

import com.android.billingclient.api.Purchase;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.assertEquals;
import static ro.pontes.englishromaniandictionary.PremiumPurchasePolicy.Action.*;

public class PremiumPurchasePolicyTest {
    @Test public void unrelatedPurchaseDoesNotUnlockPremium() {
        assertEquals(IGNORE, PremiumPurchasePolicy.action(Collections.singletonList("other"), Purchase.PurchaseState.PURCHASED, true));
    }
    @Test public void missingProductsDoNotUnlockPremium() {
        assertEquals(IGNORE, PremiumPurchasePolicy.action(null, Purchase.PurchaseState.PURCHASED, true));
        assertEquals(IGNORE, PremiumPurchasePolicy.action(Collections.emptyList(), Purchase.PurchaseState.PURCHASED, true));
    }
    @Test public void pendingPurchaseNeverUnlocksEvenIfAcknowledged() {
        assertEquals(PENDING, PremiumPurchasePolicy.action(Collections.singletonList("erd.premium"), Purchase.PurchaseState.PENDING, false));
        assertEquals(PENDING, PremiumPurchasePolicy.action(Collections.singletonList("erd.premium"), Purchase.PurchaseState.PENDING, true));
    }
    @Test public void unspecifiedStateDoesNotUnlock() {
        assertEquals(IGNORE, PremiumPurchasePolicy.action(Collections.singletonList("erd.premium"), Purchase.PurchaseState.UNSPECIFIED_STATE, true));
    }
    @Test public void unacknowledgedPurchaseRequiresAcknowledgement() {
        assertEquals(ACKNOWLEDGE, PremiumPurchasePolicy.action(Collections.singletonList("erd.premium"), Purchase.PurchaseState.PURCHASED, false));
    }
    @Test public void acknowledgedPurchaseRestoresPremium() {
        assertEquals(GRANT, PremiumPurchasePolicy.action(Collections.singletonList("erd.premium"), Purchase.PurchaseState.PURCHASED, true));
    }
    @Test public void premiumProductNeedNotBeFirst() {
        assertEquals(GRANT, PremiumPurchasePolicy.action(Arrays.asList("other", "erd.premium"), Purchase.PurchaseState.PURCHASED, true));
    }
}
