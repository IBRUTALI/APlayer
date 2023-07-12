package com.example.aplayer.data.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.aplayer.presenter.main.adapter.AdapterState

private const val SETTINGS = " com.example.aplayer.SETTINGS"

class SettingsStorage(private val context: Context) {

    private lateinit var preferences: SharedPreferences

    fun storeListStyle(state: AdapterState) {
        preferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(STYLE_STATE, state.ordinal)
        editor.apply()
    }

    fun loadListStyle(): Int {
        preferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        return preferences.getInt(STYLE_STATE, 0)
    }

    companion object {
        const val STYLE_STATE = "Style state"
    }

}