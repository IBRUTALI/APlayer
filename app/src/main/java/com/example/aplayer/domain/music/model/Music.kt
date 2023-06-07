package com.example.aplayer.domain.music.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class Music(
    val id: Int? = null,
    val artUri: Uri? = null,
    val uri: Uri? = null,
    val data: String? = null,
    val artist: String? = "Неизвестно",
    val size: String? = null,
    val name: String? = null,
    val duration: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeParcelable(artUri, flags)
        parcel.writeParcelable(uri, flags)
        parcel.writeString(data)
        parcel.writeString(artist)
        parcel.writeString(size)
        parcel.writeString(name)
        parcel.writeString(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Music> {
        override fun createFromParcel(parcel: Parcel): Music {
            return Music(parcel)
        }

        override fun newArray(size: Int): Array<Music?> {
            return arrayOfNulls(size)
        }
    }
}
