# RcloneExplorer
[![license: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://github.com/x0b/rcloneExplorer/blob/master/LICENSE) [![Github Releases](https://img.shields.io/github/downloads/x0b/rcloneExplorer/total.svg)](https://github.com/x0b/rcloneExplorer/releases) [![GitHub release](https://img.shields.io/github/v/release/x0b/rcloneExplorer?include_prereleases)](https://github.com/x0b/rcloneExplorer/releases/latest)

A Rclone based file explorer for Android

> **Security Notice:** Please make sure that you are running rcloneExplorer **1.9.0** or newer ([more details](https://github.com/x0b/rcloneExplorer/wiki/Security-Notice-201901:-Remote-contents-readable-from-local-network-when-browsing-any-remote-with-thumbnails-enabled.)).


Features
--------

Cloud Access | 256 Bit AES Encryption | Integrated Experience
:-----:|:--------------:|:-----------:
<img src="https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/cloud-computing.png?raw=true" alt="Cloud Access" width="192" /> | <img src="https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/locked-padlock.png?raw=true" alt="256 Bit AES End-to-End Encryption" width="144" /> | <img src="https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/smartphone.png?raw=true" alt="Integrated Experience" width="176"/>
Use your cloud storage as if it were a local folder | Keep your files private on any cloud provider | Open your files or send them to the cloud with other apps

### Full list
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

Screenshots
-----------
Remotes|File Editing|File Explorer|File Upload
:-----:|:--------------:|:-----------:|:---------:|
![screenshot1](https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/screenshot_1.png?raw=true)|![screenshot4](https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/screenshot_4.png?raw=true)|![screenshot2](https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/screenshot_2.png?raw=true)|![screenshot3](https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/screenshot_3.png?raw=true)

Dark Theme|File Editing|Encrypted Config| Empty Folder
:----------:|:----------:|:--------:|:-------------:|
![screenshot7](https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/screenshot_7.png?raw=true)|![screenshot8](https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/screenshot_8.png?raw=true)|![screenshot10](https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/screenshot_10.png?raw=true) | ![screenshot9](https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/screenshot_9.png?raw=true) |


Installation
------------
Grab the [latest version](https://github.com/x0b/rcloneExplorer/releases/latest) of the signed APK and install it on your phone. Only devices running Android Lollipop 5.0 and up are supported. 

| CPU architecture | Where to find | APK identifier |
|:---|:--|:---:|
|ARM 32 Bit | older or low-cost devices | ```armeabi-v7a``` |
|ARM 64 Bit | most newer devices | ```arm64-v8a``` |
|x86 32 Bit | some TV boxes and tablets | ```x86``` |
|x86_64 | some emulators | ```x86_64``` |

If you don't know which version to pick use ```rcloneExplorer-<version>-universal-release.apk```. Most devices run ARM 64 Bit, and 64 Bit devices often can also run the respective 32 bit version at lower performance.


Roadmap
------------
Note that these plans are subject to change and might not materialize completely or at all.

#### Current Version (1.10.0)
 * **New:** Proxy support (for https & http)
 * **New:** Various internal refactorings
 * **New:** The thumbnail size limit can now be set in the settings
 * **New:** Update rclone to 1.50.2
 * **Fix:** Logging settings not explaining data collection
 * **Fix:** External locations listed multiple times
 * **Fix:** OAuth not working on newer rclone versions
 * **Fix:** Crash when streaming in background

#### Next minor version
- Bug fixes

#### Next major version(s)
- Bug fixes
- Configuration dialogs for more remotes
- Rclone about ([kaczmarkiewiczp#196](https://github.com/kaczmarkiewiczp/rcloneExplorer/issues/196))
- Re-authorize existing remotes ([#6](https://github.com/x0b/rcloneExplorer/issues/6))
- Support for cli interactive commands
- Virtual Content Provider (allows access to rclone remotes from other apps)

#### Next year
- Configuration encryption ([#12](https://github.com/x0b/rcloneExplorer/issues/12))
- Reasonably regular updates of rclone
- Play store and F-Droid availability ([kaczmarkiewiczp#192](https://github.com/kaczmarkiewiczp/rcloneExplorer/issues/197))

Known Issues
------------
- OneDrive remotes can no longer be configured in the app and must be imported. You can configure them in Termux, copy the configuration file to an accessible destination and import from there. An fix is in the works and will be released in a future version.

Contributing
------------
You can contribute by reporting any bugs and errors using the [issue tracker](https://github.com/x0b/rcloneExplorer/issues). 
Pull requests are welcome, but you have to accept a small CLA to ensure license compatibility.

This is a modified build of rcloneExplorer because of little activity in the parent repo. For the parent repository, see [kaczmarkiewiczp/rcloneExplorer](https://github.com/kaczmarkiewiczp/rcloneExplorer). If you encounter an issue with a [version published here](https://github.com/x0b/rcloneExplorer/releases), open an issue  [here](https://github.com/x0b/rcloneExplorer/issues/new). 

Credits/Libraries
-----------------
- [rclone](https://github.com/rclone/rclone) - "rsync for cloud storage"
- [Jetpack AndroidX](https://developer.android.com/license)
- [Floating Action Button SpeedDial](https://github.com/leinardi/FloatingActionButtonSpeedDial) - A Floating Action Button Speed Dial implementation for Android that follows the Material Design specification.
- [Glide](https://github.com/bumptech/glide) - An image loading and caching library for Android focused on smooth scrolling.
- [Markdown View](https://github.com/falnatsheh/MarkdownView) - MarkdownView is an Android webview with the capablity of loading Markdown text or file and display it as HTML, it uses MarkdownJ and extends Android webview.
- [Material Design Icons](https://github.com/Templarian/MaterialDesign) - 2200+ Material Design Icons from the Community.
- [Recyclerview Animators](https://github.com/wasabeef/recyclerview-animators) - An Android Animation library which easily add itemanimator to RecyclerView items.
- [Toasty](https://github.com/GrenderG/Toasty) - The usual Toast, but with steroids.
- Icons from [Flaticon](https://www.flaticon.com) courtesy of [Smashicons](https://www.flaticon.com/authors/smashicons) and [Freepik](https://www.flaticon.com/authors/freepik)
