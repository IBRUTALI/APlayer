package com.example.aplayer.domain.music

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.aplayer.data.music.ProviderRepository
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.utils.secondsToTime
import io.reactivex.Single


class ProviderRepositoryImpl(private val context: Context) : ProviderRepository {

    override fun getMusic(): Single<List<Music>> {
        return Single.create { subscriber ->
            try {
                val listMusic = ArrayList<Music>()
                val contentResolver = context.contentResolver
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val audioCursor =
                    contentResolver.query(uri, null, null, null, null).use { cursor ->
                        cursor?.let {
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
                                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                                            .secondsToTime()
                                    val size =
                                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                                    val musicId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                                    val artUri = getAlbumArt(albumId)
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
                    }
            } catch (e: IllegalStateException) {
                subscriber.onError(e)
            }
        }
    }

    private fun getAlbumArt(albumId: Long): Uri {
        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        return ContentUris.withAppendedId(sArtworkUri, albumId)
    }

    private fun getMusicUriById(musicId: Long): Uri{
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId)
    }

    private fun parseMusic(music: Music): Music {
        var name = music.name?.replace("_", " ")
        name = name?.replace(".mp3", "")
        name = name?.replace(".m4a", "")
        val artist: String?
        if(music.artist == null || music.artist == "<unknown>") {
            artist = "Неизвестно"
        } else {
            artist = music.artist.replaceFirstChar { char -> char.uppercaseChar() }
            name = "$artist - $name"
        }
        return music.copy(
            name = name,
            artist = artist
        )
    }
}