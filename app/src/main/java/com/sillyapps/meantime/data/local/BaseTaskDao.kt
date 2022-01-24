package com.sillyapps.meantime.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sillyapps.meantime.data.AssembledBaseTask
import com.sillyapps.meantime.data.BaseTask
import com.sillyapps.meantime.data.SimpleBaseTask

@Dao
interface BaseTaskDao {

    @Query("select * from base_task_table where id = :baseTaskId")
    fun observeBaseTask(baseTaskId: Long): LiveData<BaseTask>

    @Query("select * from base_task_table")
    fun observeAllBaseTasks(): LiveData<List<BaseTask>>

    @Query("select * from base_task_table where id = :baseTaskId")
    suspend fun getBaseTask(baseTaskId: Long): BaseTask?

    @Insert
    suspend fun insertBaseTask(baseTask: BaseTask): Long

    @Update
    suspend fun updateBaseTask(baseTask: BaseTask)

    @Query("update base_task_table set iconResId = :iconId where id = :baseTaskId")
    suspend fun updateSimpleBaseTask(baseTaskId: Long, iconId: Int)

    @Query("select id from base_task_table where formattedName = :taskName")
    suspend fun getBaseTaskIdByName(taskName: String): Long?

    @Delete
    suspend fun deleteBaseTask(baseTask: BaseTask)

    @Query("select id, name, formattedName, iconResId from base_task_table")
    fun observeAllBaseTasksSimple(): LiveData<List<SimpleBaseTask>>

    @Transaction
    suspend fun upsert(baseTask: BaseTask): Long {
        val id = insertBaseTask(baseTask)
        if (id == -1L) {
            updateBaseTask(baseTask)
            return baseTask.id
        }
        return id
    }

    @Transaction
    @Query("select * from base_task_table")
    suspend fun getAssembledBaseTasks(): List<AssembledBaseTask>
}