### 1.12.2
* **Fix:** Crash when WebView is (temporarily) not available
* **Fix:** Crash when app is paused while in large folder
* **Fix:** Crash when moving files
* **Fix:** Crash when retrieving thumbnail on SAF storage
* **Fix:** Crash when view is not loaded in Onboarding
* **Fix:** Crashes and errors when running on wrong ABI
* **Fix:** DocumentsProvider not showing up or delaying DocumentsUI significantly
* **Fix:** DocumentsProvider showing deleted remotes
* **Fix:** Downloading file with ASCII null in path
* **Fix:** OAuth locking up when reauthorizing remote
* **Fix:** Onboarding being displayed with white background on some devices
* **Fix:** Onboarding on Android R+ will now always request permissions
* **Fix:** OneDrive will now show an error message if there is no Webbrowser available
* **Fix:** Running jobs not keeping the service alive
* **Fix:** Unreadable locations shown in file picker
* **Fix:** Update notifications for GitHub release users
* **Fix:** Validation when reading rclone config
* **Internal:** Improve logging quality

***

### 1.12.1
* **Fix:** Crash when opening a file by tapping
* **Fix:** Crash when opening logging settings and then immediately going back
* **Fix:** Failures when downloading or synchronising to internal storage or SD card
* **Fix:** Error message when remote config is missing type attribute
* **Fix:** SAF DocumentProvider not working
* **Internal:** Reduce logging noise
* **Internal:** Force stop with sigquit (rclone traces)
* **Internal:** "Log rclone errors" now also applies to rclone serve

***

### 1.12.0
* **New:** Support for accessing cloud files from other apps (SAF DocumentProvider)
* **New:** Support for all DocumentProviders
* **New:** Support for translations, with German as test language
* **Fix:** Crash after refreshing directory
* **Fix:** Crash when deleting remote
* **Fix:** Crash when lifecycle event occurs while creating link
* **Fix:** Crash when setting up local remote
* **Fix:** Crash when sharing file to RCX and cancelling immediately
* **Fix:** Crash when sharing file to RCX with missing permissions
* **Fix:** Crash when sharing file to RCX without name
* **Fix:** Crash when trying to access SAF content
* **Fix:** Crash when trying to authenticate an OAuth remote on devices with defective browser
* **Fix:** Crash when trying to stop failed Streaming
* **Fix:** Crash when updating local storage devices when discovering invalid or unknown devices
* **Fix:** FileNotFound being reported as SAF server error (SAFDAV)
* **Fix:** Local storage spaces not refreshing after changing storage permissions
* **Fix:** Notification for uploads/downloads showing garbage
* **Fix:** OAuth getting stuck ("Error creating remote")
* **Fix:** OneDrive configuration not working
* **Fix:** Password default length and generation algorithm
* **Fix:** Seafile DocumentsProvider access not working (SAFDAV)
* **Fix:** Slow deletions in large directories or containers
* **Fix:** Streaming getting stuck after failure
* **Fix:** Using technical name for remotes in some circumstances
* **Internal:** Appcenter error reporting has replaced Firebase
* **Internal:** Fix source build (rclone, GitHub actions)
* **Internal:** Support for rclone rcd
* **Internal:** Updated dependencies (gradle, java, rclone)

***

### 1.11.4
* **Fix:** Crash when sharing file to rcx contains garbage data, part II
* **Fix:** Crash when deleting remotes
* **Fix:** Crash when opening changelog or license on Android Lollipop. Note that Lollipop is no longer supported and will be removed in a future version. 
* **Fix:** Crash when generating link
* **Fix:** VLC not playing audio files
* **Fix:** Link, Sync and Download being displayed for local remotes
* **Fix:** RCX not working on some 64 Bit ARM devices. This might break again in a future version.

***

### 1.11.3
* **Fix:** Crash when selecting "Add Storage" on TVs and other devices
* **Fix:** Crash when sharing file to rcx contains garbage data
* **Fix:** Crash when using thumbnails
* **Fix:** Crash after granting storage permission, part II
* **Fix:** Crash when navigation gets confused
* **Fix:** Crash when creating config for meta-remotes

***

### 1.11.2
* **Fix:** Crash after granting storage permission
* **Fix:** Crash when canceling loading early
* **Fix:** Crash when importing config on device without ACTION_OPEN_DOCUMENT
* **Fix:** Crash when stream is detected as failed
* **Fix:** Crash when reading circular dependent config
* **Fix:** Crash reporting not working

***

### 1.11.1
* **Fix:** Local drives refreshing when nothing changed
* **Fix:** Local drives not cleaning up properly
* **Fix:** Renamed and generated remote names
* **Fix:** OAuth flag missing for Dropbox, Jottacloud and Mail.ru
* **Fix:** SD card SAF access errors being ignored
* **Fix:** Crash when streaming file fails
* **Fix:** Crash when having local drives enabled
* **Fix:** Crash when picking files for upload (Huawei only)

***

### 1.11.0
* **New:** Mega.nz config
* **New:** Google Photos config
* **New:** Renaming remotes
* **New:** DLNA serving
* **New:** Automatically manage alias remotes for local drives
* **New:** Show storage usage (rclone about)
* **New:** Reauthorize OAuth remotes
* **New:** Rclone 1.51.0
* **New:** Settings shortcut
* **New:** Android R support (preliminary)
* **Fix:** OneDrive configuration
* **Fix:** Streams now only start if available, otherwise fail loudly
* **Fix:** Crash when opening file
* **Internal**
   * Java 8 and Stream support
   * Rclone source build
   * Improved logging
   * InteractiveRunner Framework
   * Moved strings to external resource for translation
   
***

### 1.10.2
* **Fix:** Crash when opening file

***

### 1.10.1
* **Fix:** Thubnails not loading after device rotation
* **Fix:** Crash when importing rclone config
* **Fix:** Crash when dismissing invalid dialog
* **Fix:** Crash when streaming starting stream

***

### 1.10.0
* **New:** Proxy support (for https & http)
* **New:** Various internal refactorings
* **New:** The thumbnail size limit can now be set in the settings
* **New:** Update rclone to 1.50.2
* **Fix:** Logging settings not explaining data collection
* **Fix:** External locations listed multiple times
* **Fix:** OAuth not working on newer rclone versions
* **Fix:** Crash when streaming in background

***

### 1.9.2
* **New:** SAF DCA: faster directory lists & more
* **Fix:** Crash when upload from directory that is no longer accessible
* **Fix:** Creating a public link also toggles "wrap filenames"
* **Fix:** OAuth configurations not working

***

### 1.9.1
* **New:** Update to Rclone v1.49.5
* **Fix:** Crash in LoadingDialog
* **Fix:** Rclone serve not starting correctly
* **Fix:** Crash from robotest (#23)

***

### 1.9.0
* **New:** Update to Rclone v1.49.4
* **New:** Preview of Storage Access Layer for rclone (allows access to Storage Access Framework locations from rclone/rcloneExplorer)
* **New:** Support for creating union remotes
* **Fix:** Crash when no compatible streaming app is installed
* **Fix:** Crash on LoadingDialog
* **Fix:** Crash on Android 5.1.1 for remotes with time zones
* **Fix:** Crash in hide remotes dialog
* **Fix:** Security issue when using thumbnails (details see wiki)

***

### 1.8.2
* **New:** Update to Rclone v1.49.3
* **New:** Improved Android 10 support: Streaming now works with third party players
* **New:** Improved Android TV support: App no longer crashes on unsupported link types and will show up on your home screen if installed
* **New:** Support more external storage options when selecting files for upload
* **New:** Updated dependencies and migrated to AndroidX
* **Fix:** Crash when selecting inaccessible storage location

***

### 1.8.1 (previously released as 1.7.5-android10test)
* **Fix:** Rebuild with go1.13 for Android 10 testing

***

### 1.8.0 (previously released as v1.7.5)
* First Release with non-conflicting package name
* **New:** Update to Rclone v1.49.1
* **New:** Support for devices with x86_64 cpu and new Android W^X policy
* **New:** Import external config files automatically by placing them on external storage in ```Android/data/``````ca.pkay.rcloneexplorer.x0b``````/files/rclone.conf```
* **New:** Fix files with mime type application/octet-stream if they have a playable extension
* **Fix:** Crash when folder to large
* **Fix:** Onedrive config has been removed from ui because it is no longer compatible with rclone
