package com.example.aplayer.presenter.main.adapter

enum class AdapterState {
    LINEAR,
    GRID
}

fun Int.toAdapterState(): AdapterState {
    return when(this) {
        0 -> {
            AdapterState.LINEAR
        }

        else -> {
            AdapterState.GRID
        }
    }
}