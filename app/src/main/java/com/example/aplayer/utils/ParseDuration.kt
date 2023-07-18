package com.example.aplayer.utils

import java.util.concurrent.TimeUnit

fun Int?.millisecondsToTime(): String {
    return if (this != null) {
        String.format(
            "%d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(toLong()),
            TimeUnit.MILLISECONDS.toSeconds(toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(toLong()))
        )
    } else "0:00"
}

fun Long?.millisecondsToTime(): String {
    return if (this != null) {
        String.format(
            "%d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(this),
            TimeUnit.MILLISECONDS.toSeconds(this) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
        )
    } else "0:00"
}