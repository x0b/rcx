package ca.pkay.rcloneexplorer.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectoryObject {

    //private final long DIR_VALID_TIME = 600000; // 10 minutes
    //private final long DIR_VALID_TIME = 120000; // 2 minutes
    private final long DIR_VALID_TIME = 300000; // 5 minutes
    private String currentDir;
    private List<FileItem> directoryContent;
    private Map<String, List<FileItem>> directoryCache;
    private Map<String, Long> directoryValidity;

    public DirectoryObject() {
        directoryContent = new ArrayList<>();
        directoryCache = new HashMap<>();
        directoryValidity = new HashMap<>();
    }

    public void set(String path, List<FileItem> content) {
        currentDir = path;
        directoryContent = new ArrayList<>(content);
        directoryCache.put(path, new ArrayList<>(directoryContent));
        directoryValidity.put(path, System.currentTimeMillis());
    }

    public void setPath(String path) {
        currentDir = path;
        directoryContent.clear();
    }

    public void setContent(List<FileItem> content) {
        directoryContent = new ArrayList<>(content);
        directoryCache.put(currentDir, new ArrayList<>(content));
        directoryValidity.put(currentDir, System.currentTimeMillis());
    }

    public void restoreFromCache(String path) {
        currentDir = path;
        directoryContent = new ArrayList<>(directoryCache.get(path));
    }

    public void clear() {
        currentDir = "";
        directoryContent.clear();
        directoryCache.clear();
        directoryValidity.clear();
    }

    public boolean isDirectoryContentEmpty() {
        return directoryContent.isEmpty();
    }

    public String getCurrentPath() {
        return currentDir;
    }


    public List<FileItem> getDirectoryContent() {
        return directoryContent;
    }

    public boolean isContentValid() {
        return isContentValid(currentDir);
    }

    public boolean isContentValid(String path) {
        if (directoryValidity.containsKey(path)) {
            long time = System.currentTimeMillis();
            long dirTime = directoryValidity.get(path);
            return ((time - dirTime) < DIR_VALID_TIME);
        } else {
            return true;
        }
    }

    public boolean isPathInCache(String path) {
        return directoryCache.containsKey(path);
    }

    public void removePathFromCache(String path) {
        directoryCache.remove(path);
        directoryValidity.remove(path);
    }
}
