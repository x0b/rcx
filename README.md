# RcloneExplorer
[![license: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://github.com/x0b/rcloneExplorer/blob/master-x0b/LICENSE) [![Github Releases](https://img.shields.io/github/downloads/x0b/rcloneExplorer/total.svg)](https://github.com/x0b/rcloneExplorer/releases) [![GitHub release](https://img.shields.io/github/v/release/x0b/rcloneExplorer?include_prereleases)](https://github.com/x0b/rcloneExplorer/releases/latest) [![Google Play Pre-Registration](https://img.shields.io/badge/Google_Play-Pre%E2%80%93Registration-brightgreen)](https://forms.gle/5jLYhZwafx7nEfi16)

A Rclone based file explorer for Android

> **Security Notice:** Please make sure that you are running rcloneExplorer **1.9.0** or newer ([more details](https://github.com/x0b/rcloneExplorer/wiki/Security-Notice-201901:-Remote-contents-readable-from-local-network-when-browsing-any-remote-with-thumbnails-enabled.)).


Features
--------

Cloud Access | 256 Bit AES Encryption | Integrated Experience
:-----:|:--------------:|:-----------:
<img src="https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/cloud-computing.png?raw=true" alt="Cloud Access" width="192" /> | <img src="https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/locked-padlock.png?raw=true" alt="256 Bit AES End-to-End Encryption" width="144" /> | <img src="https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/smartphone.png?raw=true" alt="Integrated Experience" width="176"/>
Use your cloud storage as if it were a local folder | Keep your files private on any cloud provider | Open your files or send them to the cloud with other apps

### All features
- File Management
    - List and view files
    - Download and upload files
    - Move, rename, and delete files and folders
- Streaming & Integration
    - Stream media files
    - Serve directories over FTP, HTTP or WebDAV
- Remotes
    - Browse any type of rclone remote (with limitations on "local" remotes)
    - Create new remotes in the app
- UI
    - Dark theme
    - Customizable primary and accent colors
- Configuration
    - Import and export rclone configuration files
    - Encrypted configurations (import only)
- Platform support
    - Runs on ARM, ARM64, x86 and x64 devices
    - Runs on Android 5 or newer ([see docs](https://github.com/x0b/rcloneExplorer/wiki#android-support-roadmap))
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

Usage
------------
[See the wiki](https://github.com/x0b/rcloneExplorer/wiki).

Roadmap
------------
Note that these plans are subject to change and might not materialize completely or at all.

#### Current Version (1.10.1)
 * **Fix:** Thubnails not loading after device rotation
 * **Fix:** Crash when importing rclone config
 * **Fix:** Crash when dismissing invalid dialog
 * **Fix:** Crash when streaming starting stream

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
This app is developed by and for its community. Any contribution to improve the app is welcome, and there are multiple to contribute:
- Enable crash reporting in ```Settings > Logging > Send anonymous crash reports```.
- Reporting bugs using the [issue tracker](https://github.com/x0b/rcloneExplorer/issues).
- Proposing and voting (👍 👎) on [new features](https://github.com/x0b/rcloneExplorer/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement+sort%3Areactions-%2B1-desc) and ideas on the [issue tracker](https://github.com/x0b/rcloneExplorer/issues).
- Translating the app into a new language. Please open an issue if you are interested.
- Implementing features and fixing bugs. See the list of [available](https://github.com/x0b/rcloneExplorer/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22) and [easier](https://github.com/x0b/rcloneExplorer/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) issues. Please comment on the issue if you want to take over.

License
-----------------
### About this app
This app is released under the terms of the [GPLv3 license](https://github.com/x0b/rcloneExplorer/blob/master-x0b/LICENSE). Community contributions are licensed under the MIT license, and [CLA Assistant](https://cla-assistant.io/) will ask you to confirm [a CLA stating that](https://gist.githubusercontent.com/x0b/889f037d76706fc9e3ab8ee1c047841b/raw/67c028b19e33111428904558cfda0c01039d1574/rcloneExplorer-cla-202001) if create a PR. 

This is a modified build of rcloneExplorer because of little activity in the parent repo. For the parent repository, see [kaczmarkiewiczp/rcloneExplorer](https://github.com/kaczmarkiewiczp/rcloneExplorer). If you encounter an issue with a [version published here](https://github.com/x0b/rcloneExplorer/releases), open an issue  [here](https://github.com/x0b/rcloneExplorer/issues/new). 

If you want to convey a modified version (fork), we ask you to use a different name, app icon and package id as well as proper attribution to avoid user confusion.

### Libraries
- [rclone](https://github.com/rclone/rclone) - "rsync for cloud storage"
- [Jetpack AndroidX](https://developer.android.com/license)
- [Floating Action Button SpeedDial](https://github.com/leinardi/FloatingActionButtonSpeedDial) - A Floating Action Button Speed Dial implementation for Android that follows the Material Design specification.
- [Glide](https://github.com/bumptech/glide) - An image loading and caching library for Android focused on smooth scrolling.
- [Markdown View](https://github.com/falnatsheh/MarkdownView) - MarkdownView is an Android webview with the capablity of loading Markdown text or file and display it as HTML, it uses MarkdownJ and extends Android webview.
- [Material Design Icons](https://github.com/Templarian/MaterialDesign) - 2200+ Material Design Icons from the Community.
- [Recyclerview Animators](https://github.com/wasabeef/recyclerview-animators) - An Android Animation library which easily add itemanimator to RecyclerView items.
- [Toasty](https://github.com/GrenderG/Toasty) - The usual Toast, but with steroids.
- Icons from [Flaticon](https://www.flaticon.com) courtesy of [Smashicons](https://www.flaticon.com/authors/smashicons) and [Freepik](https://www.flaticon.com/authors/freepik)
