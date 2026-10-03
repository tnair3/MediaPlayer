# Media Player

![](https://img.shields.io/badge/In_Active_Development-ff69b4?style=for-the-badge)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=materialdesign&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

[![App Version](https://img.shields.io/badge/App_Version-1.0--alpha2-blue?style=for-the-badge)](https://img.shields.io/badge/App_Version-1.0--alpha2-blue?style=for-the-badge)
[![Build](https://img.shields.io/badge/Build-debug_2026--06--24-orange?style=for-the-badge)](https://img.shields.io/badge/Build-debug_2026--06--24-orange?style=for-the-badge)

A local media player app built using Kotlin for Android devices to allow users to listen to audio files they can upload onto the app's local storage

## Features
Here are features that you can do in the current version of the application

### Implemented
* Upload and listen to audio files
* View a library of songs and albums that have been uploaded
* Edit album details
* Favourite songs
* Shuffle play
* Playlists

### Planned
* Shuffle active queue
* Device recording and audio trimming
* Vinyls (custom playlists with a nice display)
* Aesthetic customisation settings
* Export library
* Lyric uploads for songs
* Improved upload screen

### Future Exploration
* iOS port
* Windows desktop application

## The Process
This was the first time I had developed in Kotlin using Android Studio so I began by getting a visible screen, and a list view using hardcoded song objects to be
placeholders for the library list view.

I then worked on the actual uploading section of the app, where users can upload audio files stored on their device. First I had the app simply store a path to the audio
file on device using a song database that stored filePath. Then it was changed to make a copy of the audio file in local storage and extract that metadata using 
jaudiotagger and then store that in a database.

Once a song's uploading was implemented, I then implemented the now playing screen that shows the active playing song using a ModalBottomSheet so that it would slide up from the bottom.
A miniplayer card was also implemented that appears when the now playing screen is dismissed, allowing users to access the screen and showing useful information about the current playing song. 
The aesthetics of these components changed a couple times before finally landing on what it is now.

The next part to implement was allowing users to manipulate the queue. Initially users could only see the current state of the queue - a list of songs with the current song
highlighted. The first thing to change was allowing users to move songs around in the queue which has currently been implemented with an up and down arrow on each song to move it
up or down one increment in the queue (this may be changed to follow closer to Youtube Music queue changes, where a song can be held and moved up or down). Then implementations of
an "Add to queue" and a "Play Next" was complete, where it would add the selected song and only the selected song to either the end of the current queue or to the next space
respectively.

Then favouriting songs was added, using a field in the Song data class that would be updated when the user wants a song to be favourited. These can be viewed in a separate screen
and updated dynamically using its own StateFlow in the LibraryViewModel to allow users to play and shuffle specifically those songs.

As of now these are the completed features, with some planned features and bug fixes that include fixing the active queue shuffle, and then implementing playlists, and device
audio recording.

## What I Learned
TO ADD ------

## Improvements
TO ADD ------

## Running the Project
To run the most recent complete build, simply download the APK onto an Android device and open it on that device, allowing any Google Play Protect scans to go through unti it
lets you install

To run the most recent unreleased build:
```
1. Clone the repository
2. Open the project in Android Studio: https://developer.android.com/studio
3. With USB Debugging:
  4. Connect your Android device to your desktop, enabling USB Debugging
  5. Run the build, with your Android device as the target (this may take a minute)
  6. Wait for the project to build. It will auto install and open on your device

3. Without USB Debugging:
  4. In the toolbar, select Build > Generate App Bundles or APKs > Generate APKs
  5. Allow the Gradle Build to run and the project to compile (this may take a minute)
  6. Select locate once the project has been compiled
  7. Send the app-debug.apk file onto your Android device and run the file to install
```
## Video
TO ADD ------
