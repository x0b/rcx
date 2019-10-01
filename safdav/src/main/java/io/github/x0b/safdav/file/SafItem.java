package io.github.x0b.safdav.file;

import android.net.Uri;

import java.util.List;

public interface SafItem {

    /**
     * Provide a link to retrieve this item
     * @return
     */
    String getLink();

    /**
     * Get the last modified date as RFC 850-Formatted string
     * @return RFC 850 Date string
     */
    String getLastModified();

    /**
     * Creation date if available, otherwise alias to #getLastModified
     * @return
     */
    String getCreationDate();

    /**
     * Returns the item size. If the item is not a file, 0 may be returned
     * @return item size or -1 for non-file items
     */
    String getContentLength();

    /**
     * Get the name of the file for display purposes.
     * @return name string
     */
    String getName();

    /**
     * Get an appropriate file type
     * @return document type as mime type
     */
    String getContentType();

    /**
     * Indicate if the item may contain any child item. If getChildItem.length > 0, this should be true
     * @return true if the item is a directory, otherwise false
     */
    boolean isCollection();

    /**
     * Return a version token. May be derived from or aliased to {@link #getLastModified()}
     * @return a version tag
     */
    String getETag();

    /**
     * Indicate the HTTP status to be set for the file
     * @return a status string
     */
    String getStatus();

    /**
     * Get item length
     * @return length in bytes
     */
    long getLength();

    /**
     * Return any children of this this item
     * @return a {@link List} of children, if there are any
     */
    List<? extends SafItem> getChildren();

    /**
     * Get an uri that represents this item
     * @return an {@link Uri}
     */
    Uri getUri();
}
