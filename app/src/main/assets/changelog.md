### 1.3.1
* **New:** Option to delete remotes
* **New:** Option to export rclone config file
* **New:** Option to empty trash for remotes that support it
    * Encrypted remotes (crypt) are not supported
* **New:** Firebase Crashlytics
    * Any app crashes will be reported to help fix them faster
    * No identifiable information is sent, only line of code on which the crash occurred
* **New:** Push notifications when new app version is available on GitHub (can be disabled in settings)
* **Fix:** Recreate view after theme changes
* **Update:** Upload only one file at a time (fix any possible bottlenecks)
* **Update:** Download only one file at a time (fix any possible bottlenecks)
* **Update:** Move/delete operations - view is updated after each file is moved or deleted

***

### 1.3.0
* **New:** Remote creation - ability to create new remotes right from the app!
    * Most of the rclone remotes are here
    * Amazon S3, Google Cloud Storage, and Google Drive coming soon

***

### 1.2.6
* **New:** File picker
* **Fix:** Screen orientation change going back to main screen
* **Fix:** Sorting while in selection mode
* **Fix:** Other layout fixed and app crashes

***

### 1.2.5
* **New:** Run long running tasks in a Service
* **New:** File search
* **Fix:** Caching of directory content
* **Fix:** Bugs

***

### 1.2.4
* **Update:** App shortcut icons are now adaptive and with color

***

### 1.2.3
* **New:** Files can be shared with Rclone Explorer
* **New:** Tablet layout
* **Fix:** Wait for streaming service be available before streaming

***

### 1.2.2
* **Update:** Rclone to version 1.41
* **New:** App shortcuts
* **Fix:** Color picker not working on sdk 21
* **Fix:** Rclone not getting updated

***

### 1.2.1
* **New:** Dark theme!
* **Fix:** Crash when starting app for the first time

***

### 1.2.0
* **New:** Settings!
    * **New:** Custom primary and accent colors
* **New:** Group notifications together
* **New:** "Open as" option in the menu

***

### 1.1.1
* **Fix:** Hide hash calculations for crypt remotes
* **Fix:** Crash when rclone fails

***

### 1.1.0
* **New:** Support password protected configurations
* **New:** Calculate file MD5 & SHA1
* **Fix:** Copy rclone and config file in the background
* **Improve:** Notifications
* **Update:** Libraries
