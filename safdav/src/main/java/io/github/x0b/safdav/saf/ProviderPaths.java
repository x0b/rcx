package io.github.x0b.safdav.saf;

import android.content.Context;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import io.github.x0b.safdav.file.ItemNotFoundException;
import io.github.x0b.safdav.file.SafItem;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ProviderPaths {

    private static final String TAG = "ProviderPaths";
    private static volatile ConcurrentHashMap<String, Uri> urisById;
    private static volatile ConcurrentHashMap<Uri, String> idsByUri;
    private static final String ANDROID_EXTERNAL_STORAGE_PREFIX = "content://com.android.externalstorage.documents/tree/";

    public static final String ANDROID_PROVIDER_AUTHORITY = "com.android.externalstorage.documents";

    private final Context context;
    // stores permissions by their name
    private static HashMap<String, Uri> permissionsByPath = new HashMap<>();

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
     * Create a mapping between a DAV path and a corresponding permission item. This path will be
     * later used to look up the correct permission to use for accessing that storage volume.
     * @param uri
     * @return
     */
    public static String getPathForUri(Uri uri){
        if (!ANDROID_PROVIDER_AUTHORITY.equals(uri.getAuthority())) {
            return mapUriThirdParty(uri);
        }
        String path = getNormalizedPath(uri);
        path = path.substring(0, path.length()-1);
        if("primary".equals(path) && ANDROID_PROVIDER_AUTHORITY.equals(uri.getAuthority())) {
            if(Environment.isExternalStorageRemovable()) {
                path = "SD Card";
            } else {
                path = "Internal Storage";
            }
        }
        Uri storedUri = permissionsByPath.get(path);
        if(null == storedUri || storedUri.equals(uri)) {
            permissionsByPath.put(path, uri);
        } else {
            // if the name is not unique, we need to generate a counter
            int counter = 2;
            String nameInternal = path;
            while(null != storedUri) {
                path =  nameInternal + " (" + counter++ +")";
                storedUri = permissionsByPath.get(path);
            }
            permissionsByPath.put(path, uri);
        }
        return path + "/";
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
            throw new IllegalArgumentException("You must request an actual path permission, not " + requestUri);
        }
        Uri thirdParty = getUriThirdParty(requestUri);
        if (null != thirdParty) {
            return thirdParty;
        }
        // the first path segment is the id to resolve to the underlying permission uri
        String path = requestUri.substring(1);
        int idx = requestUri.indexOf('/', 1);
        String pathExtra = "";
        // cut trailing slash
        if(idx != 1 && idx == path.length()) {
            path = requestUri.substring(1, idx);
        }
        if(idx != -1 && idx < path.length()-1) {
            path = requestUri.substring(1, idx);
            pathExtra = requestUri.substring(idx);
        }
        // try to retrieve by map
        Uri storedUri = permissionsByPath.get(path);
        if(null != storedUri) {
            Uri.Builder builder = new Uri.Builder();
            return builder
                    .scheme("content")
                    .authority(storedUri.getAuthority())
                    .path(storedUri.getPath() + pathExtra)
                    .build();
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
        path = path.substring(begin, end);
        if(path.length()-1 != path.lastIndexOf('/')) {
            path = path + '/';
        }
        return path;
    }

    private static void initUriMaps() {
        if (null == idsByUri) {
            synchronized (ProviderPaths.class) {
                if(null == idsByUri) {
                    idsByUri = new ConcurrentHashMap<>();
                    urisById = new ConcurrentHashMap<>();
                }
            }
        }
    }

    private static String mapUriThirdParty(Uri uri) {
        initUriMaps();
        synchronized (idsByUri) {
            if(!idsByUri.containsKey(uri)) {
                String uriId = uri.getAuthority() + " (" + UUID.randomUUID().toString() + ")"; //uri.getPath().replace("/", "_")
                idsByUri.put(uri, uriId);
                urisById.put(uriId, uri);
            }
            return idsByUri.get(uri) + "/";
        }
    }

    private static @Nullable Uri getUriThirdParty(String requestUri) {
        int segmentEnd = requestUri.indexOf('/', 1);
        if (-1 == segmentEnd && requestUri.length() >= 2) {
            segmentEnd = requestUri.length();
        }
        String permissionId = requestUri.substring(1, segmentEnd);
        initUriMaps();
        synchronized (idsByUri) {
            if(urisById.containsKey(permissionId)) {
                Uri uri = urisById.get(permissionId);
                if (requestUri.length() >= segmentEnd + 1) {
                    return uri.buildUpon().appendEncodedPath(requestUri.substring(segmentEnd+1)).build();
                } else {
                    return uri;
                }
            }
            return null;
        }
    }
}
