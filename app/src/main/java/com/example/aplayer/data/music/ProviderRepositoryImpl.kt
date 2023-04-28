package com.example.aplayer.data.music

import android.content.Context
import android.provider.MediaStore
import com.example.aplayer.domain.music.model.Music
import io.reactivex.Single

class ProviderRepositoryImpl(private val context: Context) : ProviderRepository {

    override fun getMusic(): Single<List<Music>> {
        return Single.create { subscriber ->
            try {
                val listMusic = ArrayList<Music>()
                val contentResolver = context.contentResolver!!
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val cursor = contentResolver.query(uri, null, null, null, null)!!.use { cursor ->
                    if (cursor.count > 0) {
                        while (cursor.moveToNext()) {
                            val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                            val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                            val data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                            val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                            val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                            val music = Music(
                                album = album,
                                artist = artist,
                                data = data,
                                name = name,
                                duration = duration
                            )
                            listMusic.add(music)
                        }
                        subscriber.onSuccess(listMusic)
                    }

                }
            } catch (e: IllegalStateException) {
                subscriber.onError(e)
            }
        }
    }
}