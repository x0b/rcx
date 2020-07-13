package ca.pkay.rcloneexplorer.SAFProvider;

import android.net.Uri;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.List;

import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Rclone;

class RcxUri {
    private static final String RCX_SCHEME = "rcx://";

    private String uri;
    private Uri parsedUri;
    private List<String> pathSegments;

    public RcxUri(String uri) {
        this.uri = uri;
        this.parsedUri = Uri.parse(uri);
        this.pathSegments = parsedUri.getPathSegments();
    }

    public RcxUri(Uri uri) {
        this(uri.toString());
    }

    @NotNull
    @Override
    public String toString() {
        return uri;
    }

    public static RcxUri fromRemoteName(String remoteName) {
        return new RcxUri(RCX_SCHEME + Uri.encode(remoteName));
    }

    public String getPathForRClone() {
        StringBuilder sb = new StringBuilder();
        for (final String s : pathSegments) {
            sb.append("/");
            sb.append(Uri.decode(s));
        }
        return sb.toString();
    }

    public String getRemoteName() {
        return Uri.decode(parsedUri.getHost());
    }

    public RemoteItem getRemoteItem(Rclone rclone) {
        final String remoteName = this.getRemoteName();
        for (final RemoteItem remote : rclone.getRemotes()) {
            if (remote.getName().equals(remoteName)) {
                return remote;
            }
        }
        return null;
    }

    public RcxUri getParentRcxUri() {
        if (pathSegments.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder(
            parsedUri.getScheme() + "://" + Uri.encode(parsedUri.getHost())
        );
        for (String segment : pathSegments.subList(0, pathSegments.size() - 1)) {
            sb.append("/" + segment);
        }
        return new RcxUri(sb.toString());
    }

    public RcxUri getChildRcxUri(String unencodedFilename) {
        return new RcxUri(
            Uri.withAppendedPath(parsedUri, Uri.encode(unencodedFilename))
        );
    }

    public String getFileName() {
        return pathSegments.get(pathSegments.size() - 1);
    }

    public FileItem getFileItem(Rclone rclone) throws FileNotFoundException {
        // Unfortunately, rclone has no equivalent for ls' "--directory" option
        // that lists directories and not their content.
        // As a workaround, we query the parent directory of the requested document
        // and find the corresponding item within it.
        RcxUri parentRcxUri = getParentRcxUri();

        final List<FileItem> directoryContent = rclone.ls(
            getRemoteItem(rclone),
            parentRcxUri.getPathForRClone(),
            false
        );
        if (directoryContent == null) {
            throw new FileNotFoundException("Couldn't query document document.");
        }

        final String fileName = getFileName();
        for (final FileItem fileItem : directoryContent) {
            if (fileItem.getName().equals(fileName)) {
                return fileItem;
            }
        }
        throw new FileNotFoundException("Couldn't find document in remote document.");
    }
}
