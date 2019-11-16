package io.github.x0b.safdav.saf;

import android.content.UriPermission;
import android.net.Uri;
import io.github.x0b.safdav.file.SafItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static io.github.x0b.safdav.saf.ProviderPaths.getNormalizedPath;
import static io.github.x0b.safdav.saf.ProviderPaths.getPathForUri;

public class SafPermissionItem implements SafItem {

    private static final String TAG = "SafPermissionItem";

    private UriPermission permission;
    private List<SafPermissionItem> permissions;
    private final SimpleDateFormat rfc1123;
    private final SimpleDateFormat rfc3339;

    /**
     * Create a permission holder. Must supply one or more permissions
     * @param permission a single @{@link UriPermission}
     */
    public SafPermissionItem(UriPermission permission){
        this.permission = permission;
        rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        rfc1123.setTimeZone(TimeZone.getTimeZone("GMT"));

        rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        rfc3339.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Create a permission holder. Must supply one or more permissions
     * @param permissions a {@link List} of permissions
     */
    public SafPermissionItem(List<UriPermission> permissions){
        this.permissions = new ArrayList<>();
        for(UriPermission permission : permissions){
            this.permissions.add(new SafPermissionItem(permission));
        }
        rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        rfc1123.setTimeZone(TimeZone.getTimeZone("GMT"));

        rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        rfc3339.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public String getLink() {
        if(null == permission){
            return "";
        }
        return getPathForUri(getUri());
    }

    @Override
    public String getLastModified() {
        if(null == permission){
            return rfc1123.format(new Date(0));
        }
        return rfc1123.format(new Date(permission.getPersistedTime()));
    }

    @Override
    public String getCreationDate() {
        if(null == permission){
            return rfc3339.format(new Date(0));
        }
        return rfc3339.format(new Date(permission.getPersistedTime()));
    }

    @Override
    public String getContentLength() {
        return "-1";
    }

    @Override
    public String getName() {
        if(null == permission){
            return "Storage Permissions";
        }
        return permission.getUri().getEncodedPath();
    }

    @Override
    public String getContentType() {
        return "application/vnd.android.UriPermission";
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public String getETag() {
        if(null == permission){
            return "";
        }
        return Long.toString(permission.getPersistedTime());
    }

    @Override
    public String getStatus() {
        return "HTTP/1.1 200 OK";
    }

    @Override
    public long getLength() {
        return -1;
    }

    @Override
    public List<? extends SafItem> getChildren() {
        if(null != permissions){
            return permissions;
        }
        throw new UnsupportedOperationException("Individual permissions don't have any children");
    }

    @Override
    public Uri getUri() {
        return permission.getUri();
    }
}
