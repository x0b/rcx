package io.github.x0b.safdav.provider;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import androidx.annotation.AnyRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class SingleRootProvider extends DocumentsProvider {

    protected static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            DocumentsContract.Root.COLUMN_SUMMARY
    };

    protected static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
    };

    protected static final String[] DEFAULT_REMOTE_PROJECTION = new String[]{
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_ICON
    };

    private final String rootId;
    private final String rootDocId;

    public SingleRootProvider(String rootId, String rootDocId) {
        this.rootId = rootId;
        this.rootDocId = rootDocId;
    }

    protected final Cursor buildRoot(@AnyRes int icon, @StringRes int title, String summary, @RootFlags int flags) {
        try (MatrixCursor result = new MatrixCursor(DEFAULT_ROOT_PROJECTION)) {
            MatrixCursor.RowBuilder row = result.newRow();
            row.add(DocumentsContract.Root.COLUMN_ROOT_ID, rootId);
            row.add(DocumentsContract.Root.COLUMN_ICON, icon);
            row.add(DocumentsContract.Root.COLUMN_TITLE, getContext().getString(title));
            row.add(DocumentsContract.Root.COLUMN_FLAGS, flags);
            row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, rootDocId);
            row.add(DocumentsContract.Root.COLUMN_SUMMARY, summary);
            return result;
        }
    }

    public final Cursor createSingleRow(@Nullable String[] columnNames, @NonNull Object... values) {
        if(null == columnNames) {
            columnNames = DEFAULT_DOCUMENT_PROJECTION;
        }
        if(columnNames.length != values.length) {
            throw new IllegalArgumentException("Document row value length must match column name length");
        }
        try (MatrixCursor result = new MatrixCursor(columnNames)) {
            result.addRow(values);
            return result;
        }
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @IntDef(
            flag = true,
            value = {DocumentsContract.Root.FLAG_LOCAL_ONLY,
                    DocumentsContract.Root.FLAG_SUPPORTS_SEARCH,
                    DocumentsContract.Root.FLAG_SUPPORTS_CREATE,
                    DocumentsContract.Root.FLAG_SUPPORTS_IS_CHILD,
                    DocumentsContract.Root.FLAG_SUPPORTS_RECENTS}
    )
    @Retention(RetentionPolicy.SOURCE)
    public @interface RootFlags {
    }
}
