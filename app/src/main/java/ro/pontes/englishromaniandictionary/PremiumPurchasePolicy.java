package ro.pontes.englishromaniandictionary;

import com.android.billingclient.api.Purchase;
import java.util.List;

/** Pure entitlement decision; no network/UI and no mutation of cached Premium state. */
final class PremiumPurchasePolicy {
    static final String PRODUCT_ID = "erd.premium";
    enum Action { IGNORE, PENDING, ACKNOWLEDGE, GRANT }

    static Action action(List<String> products, int state, boolean acknowledged) {
        if (products == null || !products.contains(PRODUCT_ID)) return Action.IGNORE;
        if (state == Purchase.PurchaseState.PENDING) return Action.PENDING;
        if (state != Purchase.PurchaseState.PURCHASED) return Action.IGNORE;
        return acknowledged ? Action.GRANT : Action.ACKNOWLEDGE;
    }

    private PremiumPurchasePolicy() {}
}
