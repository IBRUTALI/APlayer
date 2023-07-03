package com.example.aplayer.domain.music.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class Music(
    val id: Int? = null,
    val artUri: String? = null,
    val uri: String? = null,
    val data: String? = null,
    val artist: String? = "Неизвестно",
    val size: String? = null,
    val name: String? = null,
    val duration: String? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    companion object CREATOR : Parcelable.Creator<Music> {
        override fun createFromParcel(parcel: Parcel): Music {
            return Music(parcel)
        }

        override fun newArray(size: Int): Array<Music?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        TODO("Not yet implemented")
    }
}