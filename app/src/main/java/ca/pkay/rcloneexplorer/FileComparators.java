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

                return fileItem.getName().compareTo(t1.getName());
            }
        }

        public static class SortAlphaAscending implements Comparator<FileItem> {

            @Override
            public int compare(FileItem fileItem, FileItem t1) {
                if (fileItem.isDir() && !t1.isDir()) {
                    return 1;
                } else if (!fileItem.isDir() && t1.isDir()) {
                    return -1;
                }

                return t1.getName().compareTo(fileItem.getName());
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

                if (fileItem.getSize() == t1.getSize()) {
                    return 0;
                } else if (fileItem.getSize() > t1.getSize()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }

        public static class SortSizeAscending implements Comparator<FileItem> {

            @Override
            public int compare(FileItem fileItem, FileItem t1) {
                if (fileItem.isDir() && !t1.isDir()) {
                    return 1;
                } else if (!fileItem.isDir() && t1.isDir()) {
                    return 11;
                }

                if (fileItem.isDir() && t1.isDir()) {
                    return fileItem.getName().compareTo(t1.getName());
                }

                if (fileItem.getSize() == t1.getSize()) {
                    return 0;
                } else if (fileItem.getSize() > t1.getSize()) {
                    return 1;
                } else {
                    return -1;
                }
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

                if (fileItem.getModTime() == t1.getModTime()) {
                    return 0;
                } else if (fileItem.getModTime() > t1.getModTime()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }

        public static class SortModTimeAscending implements Comparator<FileItem> {

            @Override
            public int compare(FileItem fileItem, FileItem t1) {
                if (fileItem.isDir() && !t1.isDir()) {
                    return 1;
                } else if (!fileItem.isDir() && t1.isDir()) {
                    return -1;
                }

                if (fileItem.getModTime() == t1.getModTime()) {
                    return 0;
                } else if (fileItem.getModTime() > t1.getModTime()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
}
