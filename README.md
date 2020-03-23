# RCX - Rclone for Android
[![license: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://github.com/x0b/rcloneExplorer/blob/master-x0b/LICENSE) [![Github Releases](https://img.shields.io/github/downloads/x0b/rcloneExplorer/total.svg)](https://github.com/x0b/rcloneExplorer/releases) [![GitHub release](https://img.shields.io/github/v/release/x0b/rcloneExplorer?include_prereleases)](https://github.com/x0b/rcloneExplorer/releases/latest) [![Google Play Pre-Registration](https://img.shields.io/badge/Google_Play-Pre%E2%80%93Registration-brightgreen)](https://forms.gle/5jLYhZwafx7nEfi16)

A cloud file manager, powered by rclone. 

🎉 We have rebranded to **RCX - Rclone for Android**. [Read more about the changes on the new website](https://x0b.github.io/posts/upcoming-changes-202001/).

Features
--------

Cloud Access | 256 Bit AES Encryption | Integrated Experience
:-----:|:--------------:|:-----------:
<img src="https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/cloud-computing.png?raw=true" alt="Cloud Access" width="192" /> | <img src="https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/locked-padlock.png?raw=true" alt="256 Bit AES End-to-End Encryption" width="144" /> | <img src="https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/smartphone.png?raw=true" alt="Integrated Experience" width="176"/>
Use your cloud storage like a local folder. | Keep your files private on any cloud provider with crypt remotes. | Don't give up features or comfort just because it runs on a phone.

- **File Management** (list, view, download, upload, move, rename, delete files and folders)
- **Streaming** (Stream media files, serve files and directories over FTP, HTTP, WebDAV or DLNA)
- **Integration** (Access local storage devices and share files with the application to store them on a remote)
- **Many cloud storage providers** (all via rclone config import, some without)
- **Material Design** (Dark theme, custom primary and accent colors)
- **All architectures** (runs on ARM, ARM64, x86 and x64 devices, Android 7+ / 5+)
- **Storage Access Framework (SAF)** ([see docs](https://github.com/x0b/rcloneExplorer/wiki#adding-local-storage-saf)) for SD card and USB device access.

Screenshots
-----------
Manage Storage|Upload Files|Explore Files|Manage Files
:-----:|:--------------:|:-----------:|:---------:|
![screenshot1](https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/screenshot_1.png?raw=true)|![screenshot3](https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/screenshot_3.png?raw=true)|![screenshot7](https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/screenshot_7.png?raw=true)|![screenshot8](https://github.com/x0b/rcloneExplorer/blob/master-x0b/docs/screenshot_8.png?raw=true)

Installation
------------
Grab the [latest version](https://github.com/x0b/rcloneExplorer/releases/latest) of the signed APK and install it on your phone. You can also [register for the closed Google Play testing group](https://forms.gle/5jLYhZwafx7nEfi16), the [Google Play Beta](https://play.google.com/apps/testing/io.github.x0b.rcx), or in the near future, F-Droid.

| CPU architecture | Where to find | APK identifier |
|:---|:--|:---:|
|ARM 32 Bit | older devices | ```armeabi-v7a``` |
|**ARM 64 Bit** | **most devices** | ```arm64-v8a``` |
|Intel/AMD 32 Bit | some TV boxes and tablets | ```x86``` |
|Intel/AMD 64 Bit | some emulators | ```x86_64``` |

If you don't know which version to pick use ```rcx-<version>-universal-release.apk```. Most devices run ARM 64 Bit, and 64 Bit devices often can also run the respective 32 bit version at lower performance. The app runs on any CTS phone, tablet or TV with Android 5 or newer, but is only tested on Android 7 and newer.

Usage
------------
[See the wiki](https://github.com/x0b/rcloneExplorer/wiki).

Roadmap
------------
Note that these plans are subject to change and might not materialize completely or at all.

#### Current Version (1.11.1)
##### Fixes
 * Local drives refreshing when nothing changed
 * Local drives not cleaning up properly
 * Renamed and generated remote names
 * OAuth flag missing for Dropbox, Jottacloud and Mail.ru
 * SD card SAF access errors being ignored
 * Crash when streaming file fails
 * Crash when having local drives enabled
 * Crash when picking files for upload (Huawei only)

#### Next minor version
- Bug fixes

#### Next major version(s)
- Bug fixes
- Configuration dialogs for more remotes
- Virtual Content Provider (allows access to rclone remotes from other apps)

#### Next year
- Configuration encryption ([#12](https://github.com/x0b/rcloneExplorer/issues/12))
- Reasonably regular updates of rclone

Known Issues
------------
- Reauthorization of OAuth remotes shows intermittent failures. This is currently under investigation.

Contributing
------------
This app is developed by and for its community. Any contribution to improve the app is welcome, and there are multiple to contribute:
- Enable crash reporting in ```Settings > Logging > Send anonymous crash reports```.
- Reporting bugs using the [issue tracker](https://github.com/x0b/rcloneExplorer/issues).
- Proposing and voting (👍 👎) on [new features](https://github.com/x0b/rcloneExplorer/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement+sort%3Areactions-%2B1-desc) and ideas on the [issue tracker](https://github.com/x0b/rcloneExplorer/issues).
- Translating the app into a new language. Please [open an issue](https://github.com/x0b/rcloneExplorer/issues/new) or sent an email to [x0bdev@gmail.com](mailto:x0bdev@gmail.com) if you are interested.
- Implementing features and fixing bugs. See the list of [available](https://github.com/x0b/rcloneExplorer/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22) and [easier](https://github.com/x0b/rcloneExplorer/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) issues. Please comment on the issue if you want to take over.
- Not all enhancements must be new features. If you find yourself wanting to do something with the app but you are unsure how to do it, or you think it is needlessly complicated or might not be possible, then just ask on the [issue tracker](https://github.com/x0b/rcloneExplorer/issues), which may spark a new feature/enhancement request.
- You might also find help from other members of the larger rclone community on the [rclone forum](https://forum.rclone.org/) or the [subreddit](https://www.reddit.com/r/rclone/).

License
-----------------
### About this app
This app is released under the terms of the [GPLv3 license](https://github.com/x0b/rcloneExplorer/blob/master-x0b/LICENSE). Community contributions are licensed under the MIT license, and [CLA Assistant](https://cla-assistant.io/) will ask you to confirm [a CLA stating that](https://gist.githubusercontent.com/x0b/889f037d76706fc9e3ab8ee1c047841b/raw/67c028b19e33111428904558cfda0c01039d1574/rcloneExplorer-cla-202001) if create a PR.

This is a fork of rcloneExplorer by [Patryk Kaczmarkiewicz](https://github.com/kaczmarkiewiczp). For the parent repository, see [kaczmarkiewiczp/rcloneExplorer](https://github.com/kaczmarkiewiczp/rcloneExplorer). If you want to convey a modified version (fork), we ask you to use a different name, app icon and package id as well as proper attribution to avoid user confusion.

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
