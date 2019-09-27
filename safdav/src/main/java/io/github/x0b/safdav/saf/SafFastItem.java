package io.github.x0b.safdav.saf;

import android.net.Uri;
import android.util.Log;
import io.github.x0b.safdav.file.SafItem;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SafFastItem implements SafItem {

    // TODO: figure out if this is useful, especially for child accessing child elements vs. memory overhead
    private final Uri uri;
    private final String name;
    private final String contentType;
    private final long lastModified;
    private final long size;
    private final boolean isCollection;
    private final List<? extends SafItem> items;

    // TODO: figure out if thread safety is required
    private static final SimpleDateFormat rfc1123;
    private static final SimpleDateFormat rfc3339;

    static {
        rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        rfc1123.setTimeZone(TimeZone.getTimeZone("GMT"));

        rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        rfc3339.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public SafFastItem(Uri uri, String name, String contentType, long lastModified, long size, boolean isCollection) {
        this(uri, name, contentType, lastModified, size, isCollection, null);
    }

    public SafFastItem(Uri uri, String name, String contentType, long lastModified, long size, boolean isCollection, List<? extends SafItem> items) {
        this.uri = uri;
        this.name = name;
        this.contentType = contentType;
        this.lastModified = lastModified;
        this.size = size;
        this.isCollection = isCollection;
        this.items = items;
    }

    @Override
    public String getLink() {
        if(name == null) return "";
        String encodedName;
        try {
            encodedName = URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20");
            if(isCollection) {
                encodedName = encodedName + '/';
            }
            return encodedName;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getLastModified() {
        return rfc1123.format(new Date(lastModified));
    }

    @Override
    public String getCreationDate() {
        return rfc3339.format(new Date(lastModified));
    }

    @Override
    public String getContentLength() {
        return Long.toString(size);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContentType() {
        if(isCollection) {
            return "inode/directory";
        } else if(null != contentType) {
            return contentType;
        } else {
            return "application/octet-stream";
        }
    }

    @Override
    public boolean isCollection() {
        return isCollection;
    }

    @Override
    public String getETag() {
        return getLastModified();
    }

    @Override
    public String getStatus() {
        return "HTTP/1.1 200 OK";
    }

    @Override
    public long getLength() {
        return size;
    }

    @Override
    public List<? extends SafItem> getChildren() {
        if(null != items) {
            return items;
        } else {
            Log.e("SafFastItem", "Child queries not supported");
            return new ArrayList<>();
        }
    }

    @Override
    public Uri getUri() {
        return uri;
    }
}
