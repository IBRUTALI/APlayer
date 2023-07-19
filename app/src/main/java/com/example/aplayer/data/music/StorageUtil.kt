package com.example.aplayer.data.music

import android.content.Context
import android.content.SharedPreferences
import com.example.aplayer.domain.music.model.Music
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


private const val STORAGE = " com.example.aplayer.STORAGE"

class StorageUtil(private val context: Context) {

    private lateinit var preferences: SharedPreferences

    fun storeAudio(arrayList: ArrayList<Music>) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val gson = Gson()
        val json = gson.toJson(arrayList)
        editor.putString(MUSIC_LIST, json)
        editor.apply()
    }

    fun loadAudio(): ArrayList<Music> {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = preferences.getString(MUSIC_LIST, null)
        val type: Type = object : TypeToken<ArrayList<Music>>() {}.type
        return gson.fromJson(json, type)
    }

    fun storeAudioIndex(index: Int) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(CURRENT_POSITION, index)
        editor.apply()
    }

    fun loadAudioIndex(): Int {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        return preferences.getInt(CURRENT_POSITION, -1) //return -1 if no data found
    }

    fun clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }

    fun storeIsPlayingPosition(boolean: Boolean) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(IS_PLAYING_POSITION, boolean)
        editor.apply()
    }

    fun storeCurrentDuration(duration: Int) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(CURRENT_DURATION, duration)
        editor.apply()
    }

    fun loadCurrentDuration(): Int {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        return preferences.getInt(CURRENT_DURATION, 0)
    }

    fun isPlayingPosition(): Boolean {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        return preferences.getBoolean(IS_PLAYING_POSITION, false)
    }

    companion object {
        const val IS_PLAYING_POSITION = "Is playing position"
        const val CURRENT_POSITION = "Current position"
        const val MUSIC_LIST = "Music list"
        const val CURRENT_DURATION = "Current duration"
    }

}