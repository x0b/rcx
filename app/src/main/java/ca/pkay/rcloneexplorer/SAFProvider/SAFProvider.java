package ca.pkay.rcloneexplorer.SAFProvider;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Rclone;

public final class SAFProvider extends DocumentsProvider {
    private static final String TAG = "SAFProvider";

    private static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
        DocumentsContract.Root.COLUMN_ROOT_ID,
        DocumentsContract.Root.COLUMN_MIME_TYPES,
        DocumentsContract.Root.COLUMN_FLAGS,
        DocumentsContract.Root.COLUMN_ICON,
        DocumentsContract.Root.COLUMN_TITLE,
        DocumentsContract.Root.COLUMN_SUMMARY,
        DocumentsContract.Root.COLUMN_DOCUMENT_ID,
        DocumentsContract.Root.COLUMN_AVAILABLE_BYTES,
    };
    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        DocumentsContract.Document.COLUMN_FLAGS,
        DocumentsContract.Document.COLUMN_SIZE,
    };
    private static final int SUPPORTED_DOCUMENT_FLAGS =
        DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE
        | DocumentsContract.Document.FLAG_SUPPORTS_DELETE
        | DocumentsContract.Document.FLAG_SUPPORTS_MOVE
        | DocumentsContract.Document.FLAG_SUPPORTS_RENAME
        | DocumentsContract.Document.FLAG_SUPPORTS_WRITE;


    private Rclone rclone;
    private Context context;

    @Override
    public boolean onCreate() {
        context = this.getContext();
        if (context == null) {
            return false;
        }
        rclone = new Rclone(context);
        return true;
    }

    @Override
    public Cursor queryRoots(String[] projection) {
        if (projection == null) {
            projection = DEFAULT_ROOT_PROJECTION;
        }

        List<RemoteItem> remotes = rclone.getRemotes();

        final MatrixCursor result = new MatrixCursor(projection, remotes.size());

        if (remotes.size() == 0) {
            return result;
        }

        for (RemoteItem remote : remotes) {
            if (remote.isRemoteType(RemoteItem.LOCAL)) {
                continue;
            }

            RcxUri rcxUri = RcxUri.fromRemoteName(remote.getName());
            Log.d(TAG, "Adding root " + rcxUri.toString() + ".");

            final MatrixCursor.RowBuilder row = result.newRow();
            row.add(DocumentsContract.Root.COLUMN_ROOT_ID, rcxUri);
            row.add(DocumentsContract.Root.COLUMN_SUMMARY, remote.getName());
            row.add(
                DocumentsContract.Root.COLUMN_FLAGS,
                DocumentsContract.Root.FLAG_SUPPORTS_CREATE
            );
            row.add(DocumentsContract.Root.COLUMN_TITLE, "RClone");
            row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, rcxUri);
            row.add(DocumentsContract.Root.COLUMN_MIME_TYPES, null);
            row.add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, null);
            row.add(DocumentsContract.Root.COLUMN_ICON, remote.getRemoteIcon());
        }

        return result;
    }

    private void includeFileItem(MatrixCursor result, FileItem file, RcxUri parentRcxUri) {
        final String fileName = file.getName();
        final RcxUri rcxUri = parentRcxUri.getChildRcxUri(fileName);
        Log.d(TAG, "Adding document " + rcxUri.toString());

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, rcxUri.toString());
        row.add(
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            file.isDir() ? DocumentsContract.Document.MIME_TYPE_DIR : file.getMimeType()
        );
        row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, file.getName());
        row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, file.getModTime());
        row.add(DocumentsContract.Document.COLUMN_FLAGS, SUPPORTED_DOCUMENT_FLAGS);
        row.add(DocumentsContract.Document.COLUMN_SIZE, file.isDir() ? null : file.getSize());
    }

    @Override
    public Cursor queryChildDocuments(String parentUri, String[] projection, String sortOrder) throws FileNotFoundException {
        if (projection == null) {
            projection = DEFAULT_DOCUMENT_PROJECTION;
        }

        final MatrixCursor result = new MatrixCursor(projection) {
            @Override
            public Bundle getExtras() {
                Bundle bundle = new Bundle();
                bundle.putBoolean(DocumentsContract.EXTRA_LOADING, true);
                return bundle;
            }
        };

        RcxUri parentRcxUri = new RcxUri(parentUri);

        Log.d(TAG, "Querying child documents from URI " + parentRcxUri.toString());
        final List<FileItem> fileItems = rclone.ls(
            parentRcxUri.getRemoteItem(rclone),
            parentRcxUri.getPathForRClone(),
            false
        );
        if (fileItems == null) {
            throw new FileNotFoundException("rclone call failed.");
        }

        for (final FileItem file : fileItems) {
            includeFileItem(result, file, parentRcxUri);
        }

        return result;
    }

    @Override
    public Cursor queryDocument(String uri, String[] projection) throws FileNotFoundException {
        if (projection == null) {
            projection = DEFAULT_DOCUMENT_PROJECTION;
        }

        final MatrixCursor result = new MatrixCursor(projection, 0);

        RcxUri rcxUri = new RcxUri(uri);
        RcxUri parentUri = rcxUri.getParentRcxUri();
        if (parentUri == null) {
            // Special case: we're at root
            final MatrixCursor.RowBuilder row = result.newRow();
            row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, uri);
            row.add(
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.MIME_TYPE_DIR
            );
            row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, rcxUri.getRemoteName());
            row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, null);
            row.add(DocumentsContract.Document.COLUMN_FLAGS, SUPPORTED_DOCUMENT_FLAGS);
            row.add(DocumentsContract.Document.COLUMN_SIZE, null);
        } else {
            includeFileItem(result, rcxUri.getFileItem(rclone), parentUri);
        }

        return result;
    }

    @Override
    public String createDocument(String parentUri, String mimeType, String displayName) throws FileNotFoundException {
        RcxUri rcxUri = new RcxUri(parentUri).getChildRcxUri(displayName);

        if (
            DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType)
            && rclone.makeDirectory(rcxUri.getRemoteItem(rclone), rcxUri.getPathForRClone())
        ) {
            return rcxUri.toString();
        }

        final Process proc = rclone.rCatFile(
            rcxUri.getRemoteItem(rclone),
            rcxUri.getPathForRClone()
        );
        final OutputStream stdin = proc.getOutputStream();
        try {
            stdin.close();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Got exception during document creation.", e);
        }

        if (proc.exitValue() == 0) {
            return rcxUri.toString();
        }

        throw new FileNotFoundException(
            "Couldn't create document at URI " + rcxUri.toString() + "."
        );
    }

    @Override
    public ParcelFileDescriptor openDocument(
        String uri,
        String mode,
        @Nullable CancellationSignal cs
    ) throws FileNotFoundException {
        Log.d(TAG, "openDocument, mode: " + mode);
        RcxUri rcxUri = new RcxUri(uri);

        ParcelFileDescriptor[] pipe;
        try {
            pipe = ParcelFileDescriptor.createPipe();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't create pipe for document " + uri + ".", e);
            throw new FileNotFoundException();
        }

        if ("r".equals(mode)) {
            final Process proc = rclone.catFile(
                rcxUri.getRemoteItem(rclone),
                rcxUri.getPathForRClone()
            );
            final InputStream is = proc.getInputStream();
            final OutputStream os = new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]);
            new BufferedTransferThread(is, os, cs).start();
            return pipe[0];
        }
        else if ("w".equals(mode)) {
            final Process proc = rclone.rCatFile(
                rcxUri.getRemoteItem(rclone),
                rcxUri.getPathForRClone()
            );
            final OutputStream os = proc.getOutputStream();
            final InputStream is = new ParcelFileDescriptor.AutoCloseInputStream(pipe[0]);
            new BufferedTransferThread(is, os, cs).start();
            return pipe[1];
        }

        throw new FileNotFoundException(
            "Cannot open uri " + uri + " in mode " + mode + "."
        );
    }

    @Override
    public void deleteDocument(String uri) throws FileNotFoundException {
        RcxUri rcxUri = new RcxUri(uri);
        Process p = rclone.deleteItems(
            rcxUri.getRemoteItem(rclone),
            rcxUri.getFileItem(rclone)
        );
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            Log.e(TAG, "Delete process was interupted.", e);
            return;
        }

        if (p.exitValue() != 0) {
            Log.e(TAG, "Couldn't delete file at URI " + uri + ".");
        }
    }

    private String mvDocument(RcxUri sourceRcxUri, RcxUri targetRcxUri) throws FileNotFoundException {
        if (!sourceRcxUri.getRemoteName().equals(
            targetRcxUri.getRemoteName()
        )) {
            throw new FileNotFoundException("Can't move remote document to another remote.");
        }

        if (!rclone.moveTo(
            sourceRcxUri.getRemoteItem(rclone),
            sourceRcxUri.getPathForRClone(),
            targetRcxUri.getPathForRClone()
        )) {
            throw new FileNotFoundException(
                "Couldn't move item file at URI " + sourceRcxUri.toString() + "."
            );
        }

        return targetRcxUri.toString();
    }

    @Override
    public String renameDocument(String sourceUri, String displayName) throws FileNotFoundException {
        RcxUri sourceRcxUri = new RcxUri(sourceUri);
        RcxUri targetRcxUri = sourceRcxUri.getParentRcxUri().getChildRcxUri(displayName);
        return mvDocument(sourceRcxUri, targetRcxUri);
    }

    @Override
    public String moveDocument(String sourceUri, String sourceParentUri, String targetParentUri) throws FileNotFoundException {
        RcxUri sourceRcxUri = new RcxUri(sourceUri);

        RcxUri targetParentRcxUrl = new RcxUri(targetParentUri);
        String fileName = sourceRcxUri.getFileName();
        RcxUri targetRcxUri = targetParentRcxUrl.getChildRcxUri(fileName);

        return mvDocument(sourceRcxUri, targetRcxUri);
    }
}
