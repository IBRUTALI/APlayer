package com.example.aplayer.presenter.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplayer.domain.service.PlayerService
import io.reactivex.Completable
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class PlayerViewModel : ViewModel() {

    private val _lastPosition = MutableLiveData(-1)
    val lastPosition: LiveData<Int> = _lastPosition
    private val _duration = MutableLiveData(0)
    val duration: LiveData<Int> = _duration
    private var job: Job? = null

    fun setStartDuration(value: Int) {
        _duration.value = value
    }

    fun setLastPosition(value: Int) {
        _lastPosition.value = value
    }
    fun launchSeekCount(isPlaying: Boolean) {
        if (!isPlaying) {
            job?.cancel()
            job = null
        } else if (job == null) {
            job = viewModelScope.launch(Dispatchers.Default) {
                while (true) {
                    delay(1000)
                    _duration.postValue(duration.value?.plus(1000) ?: 0)
                }
            }
        }
    }
}