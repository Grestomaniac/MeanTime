package com.sillyapps.meantime.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sillyapps.meantime.data.Goal
import com.sillyapps.meantime.data.TaskGoals
import com.sillyapps.meantime.data.Template

@Dao
interface TaskGoalsDao {

    @Query("select * from goal_table where id = :taskGoalsId")
    fun observeTaskGoals(taskGoalsId: Int): LiveData<TaskGoals>

    @Query("select * from goal_table")
    fun observeAllTaskGoals(): LiveData<List<TaskGoals>>

    @Query("select * from goal_table where id = :taskGoalsId")
    suspend fun getTaskGoals(taskGoalsId: Int): TaskGoals

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskGoals(taskGoals: TaskGoals): Long

    @Update
    suspend fun updateTaskGoals(taskGoals: TaskGoals)

    @Query("update goal_table set goals = :newGoals where id = :taskGoalsId")
    suspend fun updateGoals(taskGoalsId: Int, newGoals: MutableList<Goal>)

    @Query("select id from goal_table where name = :taskName")
    suspend fun getTaskGoalsIdByName(taskName: String): Int?

    @Delete
    suspend fun deleteTaskGoals(taskGoals: TaskGoals)

}