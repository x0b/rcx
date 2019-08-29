>**NOTE:**
>This is a modified build of rcloneExplorer because of little activity in the parent repo. For the parent repository, see [kaczmarkiewiczp/rcloneExplorer](https://github.com/kaczmarkiewiczp/rcloneExplorer).
>
>If you encounter an issue with a [version published here](https://github.com/x0b/rcloneExplorer/releases), [open an issue here](https://github.com/x0b/rcloneExplorer/issues/new). 


# RcloneExplorer
[![Packagist](https://img.shields.io/packagist/l/doctrine/orm.svg)](https://github.com/x0b/rcloneExplorer/blob/master/LICENSE) [![Github Releases](https://img.shields.io/github/downloads/x0b/rcloneExplorer/total.svg)](https://github.com/x0b/rcloneExplorer/master/releases) [![GitHub release](https://img.shields.io/github/release-pre/x0b/rcloneExplorer)](https://github.com/x0b/rcloneExplorer/releases/latest) [![GitHub release](https://img.shields.io/github/release/x0b/rcloneExplorer)](https://github.com/x0b/rcloneExplorer/releases/latest)

rclone explorer for Android

Features
--------
- File Management
    - List and view files
    - Dowload and upload files
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
    - Encrypted configurations
- Platform support
    - Supports ARM and x86 devices
    - Supports SDK 21+ (Lollipop 5.0)

Roadmap
------------
Note that these plans are subject to change and might not materialize completely or at all.

#### Current Version
- Fixes for various bugs
- Import without document UI
- Android 10 (Android Q) readiness updates
- Update to rclone 1.49.1
- Support for x86_64 and automatic architecture editions

#### Next Version
- Preview of Storage Access Layer for rclone (allows access to Storage Access Framework locations from rclone/rcloneExplorer)
- New remotes
- Bug fixes
- Android 10 target version updates
- AndroidX migration
- AndroidTV improvements (launcher & intent integration)


#### Next Month(s)
- Bug fixes
- Configuration dialogs for more remotes
- Android 10 target version updates
- AndroidX migration
- AndroidTV improvements


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

If you don't know which version to pick use ```app-fatapk-release.apk```.

Known Issues
------------
- OneDrive remotes can no longer be configured in the app and must be imported. You can configure them in Termux, copy the configuration file to an accessible destination and import from there.

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
