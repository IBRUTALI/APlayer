package com.example.aplayer.data.music

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.aplayer.domain.music.model.Music
import io.reactivex.Single


class ProviderRepositoryImpl(private val context: Context) : ProviderRepository {

    override fun getMusic(): Single<List<Music>> {
        return Single.create { subscriber ->
            try {
                val listMusic = ArrayList<Music>()
                val contentResolver = context.contentResolver!!
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val audioCursor = contentResolver.query(uri, null, null, null, null)!!.use { cursor ->
                    if (cursor.count > 0) {
                        while (cursor.moveToNext()) {
                            val data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                            val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                            val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                            val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                            val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                            val size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                            val artUri = getAlbumArt(data, albumId)
                            val music = Music(
                                artUri = artUri,
                                data = data,
                                artist = artist,
                                size = size,
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

    private fun getAlbumArt(filePath: String, albumId: Long): Uri {
        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        Log.d("!@#", filePath)
        return ContentUris.withAppendedId(sArtworkUri, albumId)
    }
}