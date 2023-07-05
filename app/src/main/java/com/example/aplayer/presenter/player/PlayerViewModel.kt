package com.example.aplayer.presenter.player

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayerViewModel : ViewModel() {

    private val _lastPosition = MutableLiveData(-1)
    val lastPosition = _lastPosition

}