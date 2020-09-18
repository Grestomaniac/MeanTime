package com.sillyapps.meantime.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sillyapps.meantime.data.Scheme

@Dao
interface SchemeDao {

    @Query("select * from schemes_table where id = :schemeId")
    fun observeScheme(schemeId: Int): LiveData<Scheme>

    @Query("select * from schemes_table")
    fun observeSchemes(): LiveData<List<Scheme>>

    @Query("select * from schemes_table where id = :schemeId")
    suspend fun getScheme(schemeId: Int): Scheme?

    @Query("select * from schemes_table")
    suspend fun getSchemes(): List<Scheme>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scheme: Scheme)

    @Update
    suspend fun update(scheme: Scheme)

    @Delete
    suspend fun delete(scheme: Scheme)

    @Query("DELETE FROM schemes_table")
    suspend fun deleteAllSchemes()
}