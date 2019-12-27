package io.github.x0b.safdav.file;

import android.net.Uri;

import java.io.InputStream;
import java.util.List;

/**
 * Contract for item access.
 * <br><br>
 * Keep close attention to whether they are
 * permission style Uris (uri format as granted by OPEN_DOCUMENT_TREE) or
 * document style Uris (uri format as used to access document items).
 */
public interface ItemAccess<T extends SafItem> {

    /**
     * List items in a folder or folder-like collection
     * @param uri a permission style uri representing a directory
     * @return a List&lt;T extends SafItem&gt; of contained items
     */
    List<? extends SafItem> list(Uri uri);

    /**
     * Get an InputStream for a file
     * @param documentUri a document style file {@link Uri}
     * @return file content as {@link InputStream}
     */
    InputStream readFile(T documentUri);

    /**
     * Read file meta data
     * @param uri a permission style file {@link Uri}
     * @return a concrete {@link SafItem} representing the file
     */
    T readMeta(Uri uri);

    /**
     * Write an OutputStream to a file
     * @param len file length in bytes
     * @param stream file content {@link InputStream}
     * @param documentUri document style file {@link Uri}
     */
    void writeFile(Uri documentUri, InputStream stream, long len);

    /**
     * Create a new file
     * @param uri permission style file {@link Uri} for the new file
     * @param name file name
     * @param mimeType file type, e.g. application/octet-stream
     * @param content file content {@link InputStream}
     * @param len file length in bytes
     * @return a {@link SafItem} representing the file
     */
    SafItem createFile(Uri uri, String name, String mimeType, InputStream content, long len);

    /**
     * Create a directory
     * @param uri permission style target uri
     */
    void createDirectory(Uri uri);

    /**
     * Delete an item
     * @param uri permission style uri of the item
     */
    void deleteItem(Uri uri);

    /**
     * Copy an item. If the item is a directory, all contents are copied
     * recursively. Uris are permission-style.
     * @param srcUri an {@link Uri} representing the source (file or directory)
     * @param dstUri an Uri representing the destination
     */
    void copyItem(Uri srcUri, Uri dstUri);

    /**
     * Move an item. Uris are permission-style.
     * @param item src item Uri
     * @param target target item Uri
     */
    void moveItem(Uri item, Uri target);

    /**
     * Rename an item. Uris are permission-style.
     * @param item src item Uri
     * @param target target item Uri
     */
    void renameItem(Uri item, Uri target);
}
