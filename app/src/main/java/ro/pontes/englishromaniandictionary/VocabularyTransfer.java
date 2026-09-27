package ro.pontes.englishromaniandictionary;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** User-selected documents only; never requests broad storage permissions. */
final class VocabularyTransfer {
    private final ComponentActivity activity;
    private final Runnable refresh;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final ActivityResultLauncher<String[]> open;
    private final ActivityResultLauncher<String> create;
    private long exportSection = -1;
    private boolean busy;

    // The host is ComponentActivity, not FragmentActivity; no Fragment result routing is used.
    @android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")
    VocabularyTransfer(ComponentActivity activity, Runnable refresh) {
        this.activity = activity;
        this.refresh = refresh;
        Bundle restored = activity.getSavedStateRegistry().consumeRestoredStateForKey("vocabulary_transfer");
        if (restored != null) exportSection = restored.getLong("section", -1);
        activity.getSavedStateRegistry().registerSavedStateProvider("vocabulary_transfer", () -> {
            Bundle state = new Bundle();
            state.putLong("section", exportSection);
            return state;
        });
        open = activity.registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            busy = false;
            if (uri != null) preview(uri);
        });
        create = activity.registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/octet-stream"), uri -> {
                    busy = false;
                    if (uri != null) export(uri, exportSection);
                });
    }

    void close() { worker.shutdown(); }

    void openExternal(Uri uri) {
        if (uri == null || !"content".equals(uri.getScheme())) {
            message(R.string.erd_import_error);
            return;
        }
        preview(uri);
    }

    private void ui(Runnable action) {
        activity.runOnUiThread(() -> {
            if (!activity.isFinishing() && !activity.isDestroyed()) action.run();
        });
    }

    private void message(int resource) {
        Toast.makeText(activity, resource, Toast.LENGTH_LONG).show();
    }

    private boolean begin() {
        if (busy) { message(R.string.erd_busy); return false; }
        busy = true;
        return true;
    }

    void chooseImport() {
        if (!begin()) return;
        try {
            // Providers often report .erd as text/plain or octet-stream; validate content ourselves.
            open.launch(new String[]{"*/*"});
        } catch (ActivityNotFoundException e) {
            busy = false;
            message(R.string.erd_no_picker);
        }
    }

    void chooseExport() {
        if (!begin()) return;
        worker.execute(() -> {
            List<Long> ids = new ArrayList<>();
            List<String> names = new ArrayList<>();
            try (DataBaseHelper2 helper = new DataBaseHelper2(activity);
                 Cursor rows = helper.getReadableDatabase().rawQuery(
                         "SELECT id,nume FROM sectiuni WHERE EXISTS "
                                 + "(SELECT 1 FROM vocabular WHERE idSectiune=sectiuni.id) ORDER BY nume", null)) {
                while (rows.moveToNext()) { ids.add(rows.getLong(0)); names.add(rows.getString(1)); }
                ui(() -> {
                    busy = false;
                    if (names.isEmpty()) { message(R.string.erd_no_sections); return; }
                    new AlertDialog.Builder(activity).setTitle(R.string.erd_export)
                            .setItems(names.toArray(new String[0]), (dialog, index) ->
                                    prepareExport(ids.get(index), names.get(index)))
                            .setNegativeButton(android.R.string.cancel, null).show();
                });
            } catch (Exception e) {
                ui(() -> { busy = false; message(R.string.erd_export_error); });
            }
        });
    }

    private void prepareExport(long id, String name) {
        if (!begin()) return;
        worker.execute(() -> {
            try (DataBaseHelper2 helper = new DataBaseHelper2(activity)) {
                ErdFormat.write(VocabularyFiles.readSection(helper.getReadableDatabase(), id));
                ui(() -> {
                    exportSection = id;
                    try {
                        create.launch(name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_") + ".erd");
                    } catch (ActivityNotFoundException e) {
                        busy = false;
                        message(R.string.erd_no_picker);
                    }
                });
            } catch (Exception e) {
                ui(() -> { busy = false; message(R.string.erd_export_error); });
            }
        });
    }

    private void export(Uri uri, long id) {
        if (!begin()) return;
        worker.execute(() -> {
            try (DataBaseHelper2 helper = new DataBaseHelper2(activity)) {
                String text = ErdFormat.write(VocabularyFiles.readSection(helper.getReadableDatabase(), id));
                try (OutputStream out = activity.getContentResolver().openOutputStream(uri, "wt")) {
                    if (out == null) throw new IOException("Cannot open output");
                    out.write(text.getBytes(StandardCharsets.UTF_8));
                }
                ui(() -> { busy = false; message(R.string.erd_export_done); });
            } catch (Exception e) {
                ui(() -> { busy = false; message(R.string.erd_export_error); });
            }
        });
    }

    private void preview(Uri uri) {
        if (!begin()) return;
        worker.execute(() -> {
            try {
                String name = null;
                try (Cursor cursor = activity.getContentResolver().query(uri,
                        new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) name = cursor.getString(0);
                }
                if (name == null || !name.toLowerCase(java.util.Locale.ROOT).endsWith(".erd"))
                    throw new IOException("Not an erd file");
                String section = name.substring(0, name.length() - 4).trim();
                // Existing category UI uses these delimiters internally.
                if (section.isEmpty() || section.length() > 128 || section.contains("|")
                        || section.contains(" – ") || section.equals("%")
                        || section.matches("(?s).*\\p{Cntrl}.*")) throw new IOException("Invalid section name");
                List<ErdFormat.Entry> entries;
                try (InputStream in = activity.getContentResolver().openInputStream(uri)) {
                    if (in == null) throw new IOException("Cannot open input");
                    entries = ErdFormat.read(new InputStreamReader(in, StandardCharsets.UTF_8.newDecoder()
                            .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)));
                }
                ui(() -> {
                    busy = false;
                    new AlertDialog.Builder(activity).setTitle(R.string.erd_import)
                            .setMessage(activity.getString(R.string.erd_confirm, section, entries.size()))
                            .setPositiveButton(R.string.yes, (dialog, which) -> merge(section, entries))
                            .setNegativeButton(android.R.string.cancel, null).show();
                });
            } catch (Exception e) {
                ui(() -> { busy = false; message(R.string.erd_import_error); });
            }
        });
    }

    private void merge(String section, List<ErdFormat.Entry> entries) {
        if (!begin()) return;
        message(R.string.erd_busy);
        worker.execute(() -> {
            try (DataBaseHelper2 helper = new DataBaseHelper2(activity)) {
                int added = VocabularyFiles.merge(helper.getWritableDatabase(), section, entries);
                ui(() -> {
                    busy = false;
                    refresh.run();
                    new AlertDialog.Builder(activity).setTitle(R.string.erd_import)
                            .setMessage(activity.getString(R.string.erd_import_done, added, entries.size() - added))
                            .setPositiveButton(android.R.string.ok, null).show();
                });
            } catch (Exception e) {
                ui(() -> { busy = false; message(R.string.erd_import_error); });
            }
        });
    }
}
