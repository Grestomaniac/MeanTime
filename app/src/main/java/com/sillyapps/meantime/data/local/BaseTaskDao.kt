package com.sillyapps.meantime.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sillyapps.meantime.data.BaseTask

@Dao
interface BaseTaskDao {

    @Query("select * from goal_table where id = :taskGoalsId")
    fun observeTaskGoals(taskGoalsId: Int): LiveData<BaseTask>

    @Query("select * from goal_table")
    fun observeAllTaskGoals(): LiveData<List<BaseTask>>

    @Query("select * from goal_table where id = :taskGoalsId")
    suspend fun getTaskGoals(taskGoalsId: Int): BaseTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskGoals(baseTask: BaseTask): Long

    @Update
    suspend fun updateTaskGoals(baseTask: BaseTask)

    @Update
    suspend fun updateGoals(baseTask: BaseTask)

    @Query("select id from goal_table where formattedName = :taskName")
    suspend fun getTaskGoalsIdByName(taskName: String): Int?

    @Delete
    suspend fun deleteTaskGoals(baseTask: BaseTask)

}