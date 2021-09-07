package com.sillyapps.meantime.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sillyapps.meantime.data.BaseTask
import com.sillyapps.meantime.data.SimpleBaseTask

@Dao
interface BaseTaskDao {

    @Query("select * from goal_table where id = :baseTaskId")
    fun observeBaseTask(baseTaskId: Int): LiveData<BaseTask>

    @Query("select * from goal_table")
    fun observeAllBaseTasks(): LiveData<List<BaseTask>>

    @Query("select * from goal_table where id = :baseTaskId")
    suspend fun getBaseTask(baseTaskId: Int): BaseTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBaseTask(baseTask: BaseTask): Long

    @Update
    suspend fun updateBaseTask(baseTask: BaseTask)

    @Query("update goal_table set iconResId = :iconId where id = :baseTaskId")
    suspend fun updateSimpleBaseTask(baseTaskId: Int, iconId: Int)

    @Query("select id from goal_table where formattedName = :taskName")
    suspend fun getBaseTaskIdByName(taskName: String): Int?

    @Delete
    suspend fun deleteBaseTask(baseTask: BaseTask)

    @Query("select id, name, formattedName, iconResId from goal_table")
    fun observeAllBaseTasksSimple(): LiveData<List<SimpleBaseTask>>

}