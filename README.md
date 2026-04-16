# Media Player
A local media player app built using Kotlin for Android devices to allow users to listen to audio files they can upload onto the app's local storage

App Version: 1.0-alpha1
Build: debug 2026-04-16

## Technologies
* Kotlin
* Android Studio
* Jetpack Compose

## Features
Here are features that you can do in the current version of the application

### Implemented
* Upload and listen to audio files
* View a library of songs and albums that have been uploaded
* Edit album details
* Favourite songs
* Shuffle play

### Planned
* Playlists
* Shuffle active queue
* Device recording and audio trimming
* Vinyls (custom playlists with a nice display)
* Aesthetic customisation settings
* Export library
* Lyric uploads for songs
* Improved upload screen
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
To run the most recent build, simply download the APK onto an Android device and open it on that device, allowing any Google Play Protect scans to go through unti it
lets you install

To run the most recent unreleased commit:
1. Clone the repository
2. Open the project in Android Studio: https://developer.android.com/studio
3. In the toolbar, select Build > Generate App Bundles or APKs > Generate APKs
4. Allow the Gradle Build to run and the project to compile (this may take a minute)
5. Select locate once the project has been compiled
6. Send the app-debug.apk file onto your device and run the file to install

## Video
TO ADD ------
