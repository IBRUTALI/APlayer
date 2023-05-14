package com.example.aplayer.utils

import java.util.concurrent.TimeUnit

fun parseDuration(duration: Int?): String {
   return if (duration != null){
       String.format(
           "%d:%02d",
           TimeUnit.MILLISECONDS.toMinutes(duration.toLong()),
           TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) -
                   TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration.toLong()))
       )
   } else "0:00"

}