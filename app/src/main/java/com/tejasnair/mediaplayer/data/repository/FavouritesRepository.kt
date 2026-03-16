package com.tejasnair.mediaplayer.data.repository

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore by preferencesDataStore(name = "favourites")

class FavouritesRepository(private val context: Context) {

}