package io.github.x0b.safdav.saf;

import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;
import io.github.x0b.safdav.file.SafItem;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SafFileItem implements SafItem {
    private DocumentFile file;
    private final SimpleDateFormat rfc1123;
    private final SimpleDateFormat rfc3339;

    public SafFileItem(DocumentFile file) {
        this();
        this.file = file;
    }

    protected SafFileItem(){
        rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        rfc1123.setTimeZone(TimeZone.getTimeZone("GMT"));

        rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        rfc3339.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public boolean isValid(){
        return file.exists();
    }

    public String getLink(){
        if(this.file.getName() == null) return "";
        try {
            if(isCollection()){
                return URLEncoder.encode(this.file.getName(), "UTF-8") + "/".replaceAll("\\+", "%20");
            } else {
                return URLEncoder.encode(this.file.getName(), "UTF-8").replaceAll("\\+", "%20");
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the last modified date as RFC 850-Formatted string
     * @return RFC 850 Date string
     */
    public String getLastModified(){
        return rfc1123.format(new Date(file.lastModified()));
    }

    /**
     * Creation date is not available
     * @return
     */
    public String getCreationDate(){
        return rfc3339.format(new Date(file.lastModified()));
    }

    /**
     * Returns the item size. If the item is not a file, -1 is returned
     * @return item size or -1 for non-file items
     */
    public String getContentLength(){
        if(!file.isFile()){
            return "-1";
        } else {
            return Long.toString(file.length());
        }
    }

    public long getRawLength(){
        return file.length();
    }

    public String getName(){
        return file.getName();
    }

    /**
     * Returns the document appropriate mime type
     * @return document type
     */
    public String getContentType() {
        String type = file.getType();
        if(type == null){
            type = file.isDirectory() ? "httpd/unix-directory" : "file/octet-stream";
        }
        return type;
    }

    /**
     * Returns the Collection property
     * @return true if the item is a directory, otherwise false
     */
    public boolean isCollection(){
        return file.isDirectory();
    }

    public String getETag(){
        return Long.toString(file.lastModified());
    }

    public String getStatus(){
        if(file.exists()){
            return "HTTP/1.1 200 OK";
        } else {
            return "HTTP/1.1 404 Not Found";
        }
    }

    @Override
    public long getLength() {
        return file.length();
    }

    public List<SafFileItem> getChildren(){
        List<SafFileItem> files = new ArrayList<>();
        for(DocumentFile file : file.listFiles()){
            files.add(new SafFileItem(file));
        }
        return files;
    }

    @Override
    public Uri getUri() {
        return file.getUri();
    }

    DocumentFile getFile(){
        return file;
    }

    public SafFileItem getParent() {
        return new SafFileItem(file.getParentFile());
    }
}
