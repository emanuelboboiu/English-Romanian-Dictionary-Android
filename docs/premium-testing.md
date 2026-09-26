# Premium 9.0 (94)

## Preserved benefits

- No banners on Dictionary, Vocabulary and Verbs.
- Datamuse frequent predecessors/followers, online and English-to-Romanian only.
- Other existing free resources remain free; TV remains Premium without purchase.
- Existing manual activation and saved offline entitlement are preserved.

## Implementation checks

- Only purchased erd.premium can unlock via Billing; unrelated or unspecified purchases are ignored.
- Pending purchases do not unlock. Acknowledged purchases activate immediately; unacknowledged purchases are acknowledged before activation.
- Startup, foreground return and ITEM_ALREADY_OWNED all use the same ownership processing.
- Failed/empty ownership queries do not clear saved Premium. Existing Premium installations still skip Billing; refund/revocation handling and server-side verification are not introduced in this change.
- Acknowledgement requests are deduplicated per token; failed acknowledgements retry on the next foreground query.
- Product details are fetched again before launch, including the offer token if supplied. No cached ProductDetails is reused for checkout.
- Activity destruction closes Billing; callbacks check the activity lifetime and entitlement before navigation.

## Must test with Google Play before release

The separate .debug package is useful for UI tests, but does not validate the production erd.premium product. Use an appropriately signed internal-track build for the original package and a configured license tester. Do not make real purchases as part of automated checks.

1. Complete a test purchase: one activation, correct price, ads gone on all screens, lexical benefits unlocked.
2. Cancel checkout: no activation and a clear message.
3. Test slow payment: pending stays free; complete while app is backgrounded, then return and verify activation.
4. Restore on a test install with the same Play account: matching product restored without another payment. Avoid deleting real user vocabulary/history.
5. Restore an unacknowledged purchase and verify Google acknowledgement succeeds.
6. Retry after a failed acknowledgement/connection, without a second charge.
7. Check cached Premium offline and after process restart. Check Premium information is available offline.
8. Leave/rotate/close the activity while a callback is pending; verify no crash, duplicate confirmation or unexpected checkout after returning later.
9. Verify TV and manual activation still work; do not enter the manual code on a normal test installation unless its persistent activation is desired.

Unit tests cover entitlement decisions only, not the Google Play service, billing UI, callbacks or actual payment processing.
