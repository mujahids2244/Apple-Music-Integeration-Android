package com.arhamsoft.matchranker.room

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
class User {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var token: String? =""
    var email: String =""
    var userId: String =""
    var fullname: String =""

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var profilePhoto: ByteArray?= null
}




