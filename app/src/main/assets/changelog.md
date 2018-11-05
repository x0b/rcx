### 1.7.4
* Update to Rclone v1.44
* **New:** Support for Android 9
* **New:** Serve feature
    * Support FTP
    * Remote access (off by default)
    * Optional authentication
* **Fix:** Crash when external SD card isn't mounted

***

### 1.7.3
* **Fix:** FAB appearing when bottom bar is visible
* **New:** Get notified when beta releases are available
    * Enable in Settings under Notifications (stable update notifications must be enabled)

***

### 1.7.2
* **New:** Option to hide remotes
* **Fix:** Upload bug in serve webdav
* **Fix:** Missing transfer status missing from notification

***

### 1.7.1
* **New:** Redesign settings layout
* **Update:** Libraries
* **Fix:** Some bugs with pinned navigation drawer remotes
* **Fix:** Crash when sharing files with the app
* **Fix:** Some other crashes

***

### 1.7.0
* **New:** Show progress in upload, download, and sync notifications
* **New:** Pin remotes to navigation drawer
* **New:** Option to limit transfers to Wi-Fi only
* **New:** Sync
* **New:** Add sync webdev
* **New:** When downloading, uploading, syncing, transfer only one file in parallel
* **New:** Make crash reports and app update notifications optional
* **New:** Update some icons
* **Fix:** Proper encoding of URLs for streaming
* **Fix:** Crashes

***

### 1.6.0
* **New:** Allow setting which remotes are in app shortcuts (from settings)
* **New:** Pin remotes on supported launchers (Android 8.0)
    * From the remotes screen, press the options button for a remote and click 'Add to home screen'
* **New:** Show cancel button in toolbar when items selected or in move mode
* **New:** Download files to SD card
* **New:** Upload files from SD card
* **New:** Confirmation before emptying trash
* **Fix:** Handle screen orientation change when selecting files for upload

***

### 1.5.0
* **New:** Add support for local remotes
* **New:** Add option to create Google Drive remotes
* **New:** Show detailed modified time in properties dialog
* **Fix:** Hide directory modified time in propertied dialog for unsupported remotes
* **Fix:** App update notification click action not working
* **Fix:** Consider app version name when checking if app was updated
* **Fix:** Videos and music files not streaming from crypt remote
* **Fix:** Thumbnails being wrong in some circumstances

***

### 1.4.0
* **New:** Update to Rclone v1.42
* **New:** Use MimeType supplied by Rclone v1.42
* **New:** Option to show full file names
* **New:** Allow screen rotation with files selected
* **New:** Allow screen rotation when moving files
* **New:** Option to create new folders when downloading files
* **New:** Scroll to previous directory when going back
* **New:** Option to specify Root or Home as starting point for SFTP/SSH
* **New:** Go to starting directory when cancelling file move
* **Fix:** Light Blue not getting selected as primary color
* **Fix:** Wrong password getting set from password generator
* **Fix:** New folders not getting created from Remote Destination Dialog
* **Fix:** Crash reporting
* **Fix:** Files not getting sorted in local file picker
* **Fix:** Crash when downloading files
* **Fix:** Load thumbnails for pictures and videos only (reduces app data usage)
* **Fix:** FAB getting stuck in hidden position
* **Fix:** Hide some menu options when files are selected

***

### 1.3.6
* This update contains mostly bug fixes
* **Fix:** Crash in dialogs on screen orientation change
* **Fix:** Displayed mod time
* **Fix:** Yandex remote creation
* **Fix:** Azure remote creation
* **Fix:** Hide directory mod time for remotes that don't support it
* **Fix:** Error notification after successful move

***

### 1.3.5
* **New:** Show file thumbnails (experimental feature, enable in settings)
* **New:** App updates notification updates
    * Open the GitHub releases page on notification click
    * Show app icon
* **New:** Option to cancel delete and move operations
* **New:** Password generator for crypt remote creation
* **Fix:** Delete button now working
* **Fix:** Displayed modification time
* **Fix:** Possible crash when loading remotes

***

### 1.3.4
* **New:** Add option to pin remotes to the top
* **Fix:** Possible crash when loading remotes
* **Fix:** Remove dynamic shortcut when a remote is deleted

***

### 1.3.3
* **Fix:** Infinite loading for directories with large number of files
* **Fix:** Open as not working
* **Fix:** App launching behaviour
* **Fix:** Theme of password dialog

***

### 1.3.2
* **New:** File options for individual files
* **New:** Generate public link to file/folder (rclone link)
* **New:** Support cache remotes
* **New:** Support cache remote creation
* **New:** Make anonymous crash reporting optional
* **Fix:** Sort remotes
* **Fix:** Refreshing while in search mode
* **Fix:** Detect remote type of alias/crypt remotes to show/hide specific options
* **Update:** Hint and helper text for salt in crypt config
