package com.sillyapps.meantime.data.local

import androidx.room.*
import com.sillyapps.meantime.data.Goals
import com.sillyapps.meantime.data.Scheme

@Dao
interface GoalsDao {

    @Insert
    suspend fun insert(goals: Goals): Long

    @Update
    suspend fun update(goals: Goals)

    @Delete
    suspend fun delete(goals: Goals)

}