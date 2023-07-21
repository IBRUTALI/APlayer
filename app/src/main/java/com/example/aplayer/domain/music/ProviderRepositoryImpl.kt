package com.example.aplayer.domain.music

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.aplayer.data.music.ProviderRepository
import com.example.aplayer.data.music.StorageUtil
import com.example.aplayer.domain.music.model.Music
import io.reactivex.Single


class ProviderRepositoryImpl(private val context: Context) : ProviderRepository {
    private val uri =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

    override fun getMusic(): Single<List<Music>> {
        return Single.create { subscriber ->
            try {
                val listMusic = ArrayList<Music>()
                val contentResolver = context.contentResolver
                val storageUtil = StorageUtil(context)
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Albums.ALBUM_ID,
                    MediaStore.Audio.AudioColumns.DATA,
                    MediaStore.Audio.ArtistColumns.ARTIST,
                    MediaStore.Audio.AudioColumns.TITLE,
                    MediaStore.Audio.Media.SIZE
                )

                val audioCursor =
                    contentResolver.query(uri, projection, null, null, null).use { cursor ->
                        cursor?.let {
                            if (cursor.count > 0) {
                                while (cursor.moveToNext()) {
                                    val musicId = cursor.getLong(0)
                                    val albumId =  cursor.getLong(1)
                                    val data = cursor.getString(2)
                                    val musicUri = getMusicUriById(musicId).toString()
                                    val artUri = getAlbumArt(albumId).toString()
                                    val retriever = MediaMetadataRetriever()
                                    retriever.setDataSource(context, Uri.parse(musicUri))
                                    val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                                    val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: cursor.getString(3)
                                    val name = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: cursor.getString(4)
                                    val size = cursor.getString(5)
                                    retriever.release()
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

                storageUtil.storeAudio(listMusic)
            } catch (e: IllegalStateException) {
                subscriber.onError(e)
            }
        }
    }

    private fun getAlbumArt(albumId: Long): Uri {
        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        return ContentUris.withAppendedId(sArtworkUri, albumId)
    }

    private fun getMusicUriById(musicId: Long): Uri {
        return ContentUris.withAppendedId(uri, musicId)
    }

    private fun parseMusic(music: Music): Music {
        var name = music.name?.replace("_", " ")
        name = name?.replace(".mp3", "")
        name = name?.replace(".m4a", "")
        val artist: String = if (music.artist == null || music.artist == "<unknown>") {
            "Неизвестный исполнитель"
        } else {
            name = name?.replace(music.artist + " - ", "")
            music.artist.replaceFirstChar { char -> char.uppercaseChar() }
        }
        return music.copy(
            name = name,
            artist = artist
        )
    }
}