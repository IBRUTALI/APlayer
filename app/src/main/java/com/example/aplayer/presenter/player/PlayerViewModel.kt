package com.example.aplayer.presenter.player

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayerViewModel : ViewModel() {
    val isBounded = MutableLiveData(false)
}