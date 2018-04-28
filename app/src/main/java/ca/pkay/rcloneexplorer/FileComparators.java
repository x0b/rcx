package ca.pkay.rcloneexplorer;

import java.util.Comparator;

import ca.pkay.rcloneexplorer.Items.FileItem;

public class FileComparators {

        public static class SortAlphaDescending implements Comparator<FileItem> {

            @Override
            public int compare(FileItem fileItem, FileItem t1) {
                if (fileItem.isDir() && !t1.isDir()) {
                    return -1;
                } else if (!fileItem.isDir() && t1.isDir()) {
                    return 1;
                }

                return t1.getName().toLowerCase().compareTo(fileItem.getName().toLowerCase());
            }
        }

        public static class SortAlphaAscending implements Comparator<FileItem> {

            @Override
            public int compare(FileItem fileItem, FileItem t1) {
                if (fileItem.isDir() && !t1.isDir()) {
                    return -1;
                } else if (!fileItem.isDir() && t1.isDir()) {
                    return 1;
                }

                return fileItem.getName().toLowerCase().compareTo(t1.getName().toLowerCase());
            }
        }

        public static class SortSizeDescending implements Comparator<FileItem> {

            @Override
            public int compare(FileItem fileItem, FileItem t1) {
                if (fileItem.isDir() && !t1.isDir()) {
                    return -1;
                } else if (!fileItem.isDir() && t1.isDir()) {
                    return 1;
                }

                if (fileItem.isDir() && t1.isDir()) {
                    return fileItem.getName().compareTo(t1.getName());
                }

                return Long.compare(t1.getSize(), fileItem.getSize());
            }
        }

        public static class SortSizeAscending implements Comparator<FileItem> {

            @Override
            public int compare(FileItem fileItem, FileItem t1) {
                if (fileItem.isDir() && !t1.isDir()) {
                    return -1;
                } else if (!fileItem.isDir() && t1.isDir()) {
                    return 1;
                }

                if (fileItem.isDir() && t1.isDir()) {
                    return fileItem.getName().compareTo(t1.getName());
                }

                return Long.compare(fileItem.getSize(), t1.getSize());
            }
        }

        public static class SortModTimeDescending implements Comparator<FileItem> {

            @Override
            public int compare(FileItem fileItem, FileItem t1) {
                if (fileItem.isDir() && !t1.isDir()) {
                    return -1;
                } else if (!fileItem.isDir() && t1.isDir()) {
                    return 1;
                }

                return Long.compare(t1.getModTime(), fileItem.getModTime());
            }
        }

        public static class SortModTimeAscending implements Comparator<FileItem> {

            @Override
            public int compare(FileItem fileItem, FileItem t1) {
                if (fileItem.isDir() && !t1.isDir()) {
                    return -1;
                } else if (!fileItem.isDir() && t1.isDir()) {
                    return 1;
                }

                return Long.compare(fileItem.getModTime(), t1.getModTime());
            }
        }
}
