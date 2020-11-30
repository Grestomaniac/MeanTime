package com.sillyapps.meantime.data.local

import androidx.room.TypeConverter
import com.sillyapps.meantime.data.Day
import com.sillyapps.meantime.data.Goal
import com.sillyapps.meantime.data.SimplifiedTemplate
import com.sillyapps.meantime.data.Task
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

object AppTypeConverter {

    @TypeConverter
    @JvmStatic
    fun convertStringToTaskList(data: String?): List<Task>? {
        if (data == null) return emptyList()
        val moshi = Moshi.Builder().build()

        val dataList = Types.newParameterizedType(List::class.java, Task::class.java)
        val jsonAdapter: JsonAdapter<List<Task>> = moshi.adapter(dataList)

        return jsonAdapter.fromJson(data)
    }

    @TypeConverter
    @JvmStatic
    fun convertTaskListToJson(list: List<Task>?): String {
        val moshi = Moshi.Builder().build()
        val dataList = Types.newParameterizedType(List::class.java, Task::class.java)
        val jsonAdapter: JsonAdapter<List<Task>> = moshi.adapter(dataList)

        return jsonAdapter.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun convertStringToGoalsList(data: String?): List<Goal>? {
        if (data.isNullOrEmpty()) return mutableListOf()
        val moshi = Moshi.Builder().build()

        val dataList = Types.newParameterizedType(List::class.java, Goal::class.java)
        val jsonAdapter: JsonAdapter<List<Goal>> = moshi.adapter(dataList)

        return jsonAdapter.fromJson(data)
    }

    @TypeConverter
    @JvmStatic
    fun convertGoalsListToJson(list: List<Goal>?): String {
        val moshi = Moshi.Builder().build()
        val dataList = Types.newParameterizedType(List::class.java, Goal::class.java)
        val jsonAdapter: JsonAdapter<List<Goal>> = moshi.adapter(dataList)

        return jsonAdapter.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun convertStringToOrderList(data: String?): List<SimplifiedTemplate>? {
        if (data == null) return emptyList()
        val moshi = Moshi.Builder().build()

        val dataList = Types.newParameterizedType(List::class.java, SimplifiedTemplate::class.java)
        val jsonAdapter: JsonAdapter<List<SimplifiedTemplate>> = moshi.adapter(dataList)

        return jsonAdapter.fromJson(data)
    }

    @TypeConverter
    @JvmStatic
    fun convertOrderListToJson(list: List<SimplifiedTemplate>?): String {
        val moshi = Moshi.Builder().build()
        val dataList = Types.newParameterizedType(List::class.java, SimplifiedTemplate::class.java)
        val jsonAdapter: JsonAdapter<List<SimplifiedTemplate>> = moshi.adapter(dataList)

        return jsonAdapter.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun convertStringToDay(data: String?): Day? {
        if (data == null) return null
        val moshi = Moshi.Builder().build()

        val day = Types.newParameterizedType(Day::class.java)
        val jsonAdapter: JsonAdapter<Day> = moshi.adapter(day)

        return jsonAdapter.fromJson(data)
    }

    @TypeConverter
    @JvmStatic
    fun convertDayToJson(day: Day?): String {
        val moshi = Moshi.Builder().build()
        val data = Types.newParameterizedType(Day::class.java)
        val jsonAdapter: JsonAdapter<Day> = moshi.adapter(data)

        return jsonAdapter.toJson(day)
    }
}