package io.github.x0b.safdav.saf;

import android.content.Context;
import android.content.UriPermission;
import android.net.Uri;
import android.util.Log;
import io.github.x0b.safdav.file.ItemNotFoundException;
import io.github.x0b.safdav.file.SafItem;

import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ProviderPaths {

    private static final String TAG = "ProviderPaths";
    private static final String ANDROID_EXTERNAL_STORAGE_PREFIX = "content://com.android.externalstorage.documents/tree/";

    private final Context context;

    public ProviderPaths(Context context) {
        this.context = context;
    }

    /**
     * Get the root folder (/). ProviderPaths ensures that underlying paths can be resolved using ContentProvider
     * URIs. In a future version, flattening may also be applied.
     * @return a SafItem that represents the root folder
     */
    public SafItem getSafRootDirectory() {
        List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();
        return new SafPermissionItem(permissions);
    }

    /**
     * Translates a request path into a requestable contest uri. This process assumes that <br>
     * - a hierarchical filesystem is accessed<br>
     * - this system can be mapped to urls just copying the path as sub-url path<br>
     * - the transformed url might represent a not (yet) existing object!<br>
     *  Specifically, it is only tested using com.android.externalstorage.documents
     * @param requestUri a mapped path, e.g. {@code /primary/DCIM}
     * @return an {@link Uri} representing the path as tree uri, e.g. {@code content://com.android.externalstorage.documents/tree/primary:DCIM}
     */
    public Uri getUriByMappedPath(String requestUri) {
        if(null == requestUri || '/' != requestUri.charAt(0)){
            throw new IllegalArgumentException("You must request an actual path permission");
        }

        Log.v(TAG, "Rewriting URI: " + requestUri);
        int trailingSlashPos = requestUri.lastIndexOf("/");
        int secondSlash = requestUri.indexOf("/", 1);
        // special handling for android document urls - they are normalized by using a slash instead of the internal ':'
        if(0 == trailingSlashPos) {
            requestUri = requestUri + ":";
            trailingSlashPos = requestUri.length();
        } else if(-1 != secondSlash){
            requestUri = requestUri.substring(0, secondSlash) + ':' + requestUri.substring(secondSlash+1);
            trailingSlashPos = requestUri.length();
        }
        String subPath = requestUri.substring(1, trailingSlashPos);

        // compare to existing android storage rights
        List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();
        for(UriPermission permission : permissions){

            // provider specific logic - must be adapted for other content providers
            String pathPrefix = ANDROID_EXTERNAL_STORAGE_PREFIX + subPath;
            String permissionPath = permission.getUri().toString().replace("%3A", ":");
            if(pathPrefix.contains(permissionPath)){
                // all ok, we this is a android ext doc path provider and the app has been granted access
                Log.v(TAG, "Rewrote URI to " + pathPrefix);
                Uri.Builder builder = new Uri.Builder();
                return builder
                        .scheme("content")
                        .authority("com.android.externalstorage.documents")
                        .path("/tree/" + subPath)
                        .build();
            }
        }

        Log.w(TAG, "User has no (longer) access to the specfied URI");
        throw new ItemNotFoundException();
    }

    /**
     * Converts a permission uri into a path for the top level folder structure
     *
     * content://com.android.externalstorage.documents/tree/primary:                        primary_12345678
     * content://com.paragon_software.documentproviderserver.documents/tree/root/DISK_IMG   DISK_IMG_34567890
     *
     * @param uri
     * @return
     */
    public static String getNormalizedPath(Uri uri){
        String path = uri.getPath().replace(":", "/");
        int begin = !path.contains("/tree/") ? 0 : "/tree/".length();
        int end = path.lastIndexOf(':') == path.length()-1 ? path.length()-1 : path.length();
        String folder = path.substring(begin, end);
        return folder;// + "_" + getProviderHash(uri);
    }

    public static String getProviderHash(Uri uri){
        String input = uri.getAuthority();
        byte[] bytes = input.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(bytes, 0, bytes.length);
        return Long.toHexString(checksum.getValue());
    }
}
