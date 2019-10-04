# RcloneExplorer
[![license: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://github.com/x0b/rcloneExplorer/blob/master/LICENSE) [![Github Releases](https://img.shields.io/github/downloads/x0b/rcloneExplorer/total.svg)](https://github.com/x0b/rcloneExplorer/releases) [![GitHub release](https://img.shields.io/github/v/release/x0b/rcloneExplorer?include_prereleases)](https://github.com/x0b/rcloneExplorer/releases/latest)

rclone explorer for Android

>**Note:**
>This is a modified build of rcloneExplorer because of little activity in the parent repo. For the parent repository, see [kaczmarkiewiczp/rcloneExplorer](https://github.com/kaczmarkiewiczp/rcloneExplorer).
>
>If you encounter an issue with a [version published here](https://github.com/x0b/rcloneExplorer/releases), [open an issue here](https://github.com/x0b/rcloneExplorer/issues/new). 

## SECURITY NOTICE
If you are running rcloneExplorer **1.3.5** to **1.8.2**, or AIS-synchro make sure that "Show thumbnails" in ```Settings``` > ```General``` is **disabled** and update to the latest version. If you have not enabled thumbnails, you are not affected.
More details in [Security Notice 201901: Remote contents readable from local network when browsing any remote with thumbnails enabled](https://github.com/x0b/rcloneExplorer/wiki/Security-Notice-201901-Remote-contents-readable-from-local-network-when-browsing-any-remote-with-thumbnails-enabled).

Features
--------
- File Management
    - List and view files
    - Download and upload files
    - Move, rename, and delete files and folders
- Streaming & Integration
    - Streaming media files
    - Serving directories over HTTP or WebDAV
- Remotes
    - Browse any type of rclone remote (local remotes are limited though)
    - Create new remotes in the app
- UI
    - Dark theme
    - Customizable primary and accent colors
- Configuration
    - Import and export rclone configuration files
    - Encrypted configurations (import only)
- Platform support
    - Supports ARM and x86 devices
    - Supports SDK 21+ (Lollipop 5.0)
    - Supports Storage Access Framework (SAF) ([see docs](https://github.com/x0b/rcloneExplorer/wiki#adding-local-storage-saf))

Roadmap
------------
Note that these plans are subject to change and might not materialize completely or at all.

#### Current Version (1.9.0)
 - Update rclone to 1.49.4
 - Preview of Storage Access Layer for rclone (allows access to Storage Access Framework locations from rclone/rcloneExplorer)
 - Added support for creating union remotes
 - Fix: Crash when no compatible streaming app is installed
 - Fix: Crash on LoadingDialog (#7)
 - Fix: Crash on Android 5.1.1 for remotes with time zones (#10)
 - Fix: Crash in hide remotes dialog (#13)
 - Fix: Security issue when using thumbnails (#18)

#### Next Version
- New SAF implementation for much faster directory access
- Bug fixes

#### Next Month(s)
- Bug fixes
- Configuration dialogs for more remotes
- Virtual Content Provider (allows access to rclone remotes from other apps)

#### Next Year
- Reasonably regular updates of rclone
- F-Droid availability

Screenshots
-----------

Remotes|File Editing|File Explorer|File Upload
:-----:|:--------------:|:-----------:|:---------:|
![screenshot1](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_1.png?raw=true)|![screenshot4](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_4.png?raw=true)|![screenshot2](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_2.png?raw=true)|![screenshot3](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_3.png?raw=true)

Dark Theme|File Editing|Encrypted Config| Empty Folder
:----------:|:----------:|:--------:|:-------------:|
![screenshot7](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_7.png?raw=true)|![screenshot8](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_8.png?raw=true)|![screenshot10](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_10.png?raw=true) | ![screenshot9](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_9.png?raw=true) |

Installation
------------
Grab the [latest version](https://github.com/x0b/rcloneExplorer/releases/latest) of the signed APK and install it on your phone. Only devices running Android Lollipop 5.0 and up are supported. 

If you don't know which version to pick use ```rcloneExplorer-fatapk-release.apk```.

Known Issues
------------
- OneDrive remotes can no longer be configured in the app and must be imported. You can configure them in Termux, copy the configuration file to an accessible destination and import from there.

Contributing
------------
You can contribute by reporting any bugs and errors using the [issue tracker](https://github.com/x0b/rcloneExplorer/issues). Pull requests are welcome, but you have to accept a small CLA to ensure license compatibility.

Credits/Libraries
-----------------
- [rclone](https://github.com/rclone/rclone) - "rsync for cloud storage"
- [Android Support Libraries](https://developer.android.com/topic/libraries/support-library)
- [Floating Action Button SpeedDial](https://github.com/leinardi/FloatingActionButtonSpeedDial) - A Floating Action Button Speed Dial implementation for Android that follows the Material Design specification.
- [Glide](https://github.com/bumptech/glide) - An image loading and caching library for Android focused on smooth scrolling.
- [Markdown View](https://github.com/falnatsheh/MarkdownView) - MarkdownView is an Android webview with the capablity of loading Markdown text or file and display it as HTML, it uses MarkdownJ and extends Android webview.
- [Material Design Icons](https://github.com/Templarian/MaterialDesign) - 2200+ Material Design Icons from the Community.
- [Recyclerview Animators](https://github.com/wasabeef/recyclerview-animators) - An Android Animation library which easily add itemanimator to RecyclerView items.
- [Toasty](https://github.com/GrenderG/Toasty) - The usual Toast, but with steroids.
- Icon made by [Smashicons](https://www.flaticon.com/authors/smashicons) from [Flaticon](https://www.flaticon.com)
