package com.arhamsoft.matchranker.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Query("DELETE FROM user_table")
    fun deleteUser()

    @Query("SELECT * FROM user_table ")
    fun getUser():User

    @Update
    fun updateUser(user: User)
}