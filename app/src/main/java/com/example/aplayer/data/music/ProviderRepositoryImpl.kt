package com.example.aplayer.data.music

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.example.aplayer.domain.music.model.Music
import io.reactivex.Single
import java.util.concurrent.TimeUnit


class ProviderRepositoryImpl(private val context: Context) : ProviderRepository {

    override fun getMusic(): Single<List<Music>> {
        return Single.create { subscriber ->
            try {
                val listMusic = ArrayList<Music>()
                val contentResolver = context.contentResolver!!
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val audioCursor =
                    contentResolver.query(uri, null, null, null, null)!!.use { cursor ->
                        if (cursor.count > 0) {
                            while (cursor.moveToNext()) {
                                val data =
                                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                                val albumId =
                                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                                val artist =
                                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                                val name =
                                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                                val duration =
                                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                                val size =
                                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                                val musicId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                                val artUri = getAlbumArt(data, albumId)
                                val musicUri = getMusicUriById(musicId)
                                val music = Music(
                                    artUri = artUri,
                                    uri = musicUri,
                                    data = data,
                                    artist = artist,
                                    size = size,
                                    name = name,
                                    duration = duration
                                )
                                listMusic.add(parseMusic(music))
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

    private fun getMusicUriById(musicId: Long): Uri{
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId)
    }

    private fun parseMusic(music: Music): Music {
        val duration = if(music.duration != null) String.format(
            "%d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(music.duration.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(music.duration.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(music.duration.toLong()))
        ) else "0:00"
        var name = music.name?.replace("_", " ")
        name = name?.replace(".mp3", "")
        name = name?.replace(".m4a", "")
        val artist = if(music.artist == null || music.artist == "<unknown>") {
            "Неизвестно"
        } else {
            music.artist.replaceFirstChar { char -> char.uppercaseChar() }
        }
        return music.copy(
            duration = duration,
            name = name,
            artist = artist
        )
    }
}