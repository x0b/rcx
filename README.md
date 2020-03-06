# RcloneExplorer
[![Packagist](https://img.shields.io/packagist/l/doctrine/orm.svg)](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/LICENSE) [![Github Releases](https://img.shields.io/github/downloads/kaczmarkiewiczp/rcloneExplorer/total.svg)](https://github.com/kaczmarkiewiczp/rcloneExplorer/edit/master/releases) [![GitHub release](https://img.shields.io/github/release/kaczmarkiewiczp/rcloneExplorer.svg)](https://github.com/kaczmarkiewiczp/rcloneExplorer/releases/latest)

rclone explorer for Android

Features
--------
- Allows to browse rclone remotes, including encrypted ones
- Import configuration file from rclone
- Create new remotes from the app
- Download and upload files
- Move, rename, and delete files and folders
- Create new folders
- Streaming media files
- Serving directories over HTTP or Webdav
- Dark theme
- Customizable primary and accent colors
- Supports ARM and x86 devices
- Supports SDK 21+ (Lollipop 5.0)
- Intentservice to start tasks via third party apps!

TODO
------------
- [X] Creating new remotes
  - [ ] Creating Team Drive remotes
- [X] Deleting existing remotes

Screenshots
-----------

Remotes|Encrypted Config|File Explorer|File Upload
:-----:|:--------------:|:-----------:|:---------:|
![screenshot1](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_1.png?raw=true)|![screenshot11](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_11.png?raw=true)|![screenshot2](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_2.png?raw=true)|![screenshot3](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_3.png?raw=true)

File Editing|Empty Folder|Dark Theme|Encrypted Config
:----------:|:----------:|:--------:|:-------------:|
![screenshot4](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_4.png?raw=true)|![screenshot5](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_5.png?raw=true)|![screenshot6](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_6.png?raw=true)|![screenshot10](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_10.png?raw=true)
**File Explorer**|**File Editing**|**Empty Folder**|
![screenshot7](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_7.png?raw=true)|![screenshot8](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_8.png?raw=true)|![screenshot9](https://github.com/kaczmarkiewiczp/rcloneExplorer/blob/master/screenshots/screenshot_9.png?raw=true)|

Installation
------------
Grab the [latest version](https://github.com/kaczmarkiewiczp/rcloneExplorer/releases/latest) of the signed APK and install it on your phone. Only devices running Android Lollipop 5.0 and up are supported.

- For ARM 32-bit devices download RcloneExplorer-ARM_32.apk
- For ARM 64-bit devices download RcloneExplorer-ARM_64.apk
- For x86 devices download RcloneExplorer-x86.apk
- Ultimately, RcloneExplorer.apk will work with both ARM and x86 devices.



Intentservice
-------------
This app includes the ability to launch an intent! Create a task to sync to a remote, and copy it's id (via the treedot-menu)
The intent needs the following:

| Intent        | Content       |         | 
| :------------- | :-------------: | -------------: |
| packageName      | ca.pkay.rcloneexplorer | | 
| className      | ca.pkay.rcloneexplorer.Services.TaskStartService | | 
| Action    | START_TASK | | 
| Integer Extra    | task | idOfTask |
| Boolean Extra    | notification | true or false |


Credits/Libraries
-----------------
- [Android Support Libraries](https://developer.android.com/topic/libraries/support-library)
- [Floating Action Button SpeedDial](https://github.com/leinardi/FloatingActionButtonSpeedDial) - A Floating Action Button Speed Dial implementation for Android that follows the Material Design specification.
- [Glide](https://github.com/bumptech/glide) - An image loading and caching library for Android focused on smooth scrolling.
- [Markdown View](https://github.com/falnatsheh/MarkdownView) - MarkdownView is an Android webview with the capablity of loading Markdown text or file and display it as HTML, it uses MarkdownJ and extends Android webview.
- [Material Design Icons](https://github.com/Templarian/MaterialDesign) - 2200+ Material Design Icons from the Community.
- [Recyclerview Animators](https://github.com/wasabeef/recyclerview-animators) - An Android Animation library which easily add itemanimator to RecyclerView items.
- [rclone](https://github.com/ncw/rclone) - "rsync for cloud storage"
- [Toasty](https://github.com/GrenderG/Toasty) - The usual Toast, but with steroids.
- Icon made by [Smashicons](https://www.flaticon.com/authors/smashicons) from [Flaticon](https://www.flaticon.com)
