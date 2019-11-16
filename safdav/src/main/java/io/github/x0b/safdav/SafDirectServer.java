package io.github.x0b.safdav;

import android.content.Context;
import android.net.Uri;
import io.github.x0b.safdav.file.ItemAccess;
import io.github.x0b.safdav.file.SafItem;
import io.github.x0b.safdav.saf.DocumentsContractAccess;
import io.github.x0b.safdav.saf.ProviderPaths;

import java.util.List;

/**
 * Access safdav methods without HTTP overhead. Useful for file operations where
 * both source and target are SAF-accessible.
 */
public class SafDirectServer {

    private static final String TAG = "SafDirectServer";
    private static final String ANDROID_EXTERNAL_STORAGE_PREFIX = "content://com.android.externalstorage.documents/tree/";
    private final Context context;
    private final ItemAccess itemAccess;
    private final ProviderPaths providerPaths;

    SafDirectServer(Context context) {
        this.context = context;
        itemAccess = new DocumentsContractAccess(context);
        providerPaths = new ProviderPaths(context);
    }

    /**
     * Get the contents of a a directory (or a list of permissions/drives as root)
     * @param path directory path
     * @return a {@link List<SafItem>} of items identified by that path
     */
    public List<? extends SafItem> list(String path){
        Uri uri = providerPaths.getUriByMappedPath(path);
        return itemAccess.list(uri);
    }

    /**
     * Create a directory.
     * @param path directory path
     */
    public void createDirectory(String path) {
        Uri uri = providerPaths.getUriByMappedPath(path);
        itemAccess.createDirectory(uri);
    }

    /**
     * "Upload" or "Download" a file or directory
     * @param srcPath item source path
     * @param dstPath item destination path
     */
    public void copyItem(String srcPath, String dstPath) {
        Uri srcUri = providerPaths.getUriByMappedPath(srcPath);
        Uri dstUri = providerPaths.getUriByMappedPath(dstPath);
        itemAccess.copyItem(srcUri, dstUri);
    }

    /**
     * Delete an item
     * @param path item path
     */
    public void delete(String path) {
        Uri itemUri = providerPaths.getUriByMappedPath(path);
        itemAccess.deleteItem(itemUri);
    }

    /**
     * Get a content uri from a SafDAV server uri
     * @param safDavUri the uri to decode - include any non-host path
     * @return an accessible content uri
     * @throws io.github.x0b.safdav.file.ItemNotFoundException if the uri is not accessible
     */
    public Uri getContentUri(String safDavUri) {
        return providerPaths.getUriByMappedPath(safDavUri);
    }

    public Uri getDocumentUri(String safDavUri) {
        Uri itemUri = providerPaths.getUriByMappedPath(safDavUri);
        return itemAccess.readMeta(itemUri).getUri();
    }
}
