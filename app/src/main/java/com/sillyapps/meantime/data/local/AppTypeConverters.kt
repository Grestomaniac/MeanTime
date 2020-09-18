package com.sillyapps.meantime.data.local

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class AppTypeConverters {
    companion object {

        @TypeConverter
        @JvmStatic
        fun convertStringToActivitiesList(data: String?): List<Task>? {
            if (data == null) return emptyList()
            val moshi = Moshi.Builder().build()

            val dataList = Types.newParameterizedType(List::class.java, Task::class.java)
            val jsonAdapter: JsonAdapter<List<Task>> = moshi.adapter(dataList)

            return jsonAdapter.fromJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun convertActivitiesListToJson(list: List<Task>?): String {
            val moshi = Moshi.Builder().build()
            val dataList = Types.newParameterizedType(List::class.java, Task::class.java)
            val jsonAdapter: JsonAdapter<List<Task>> = moshi.adapter(dataList)

            return jsonAdapter.toJson(list)
        }

        @TypeConverter
        @JvmStatic
        fun convertStringToOrderList(data: String?): List<Int>? {
            if (data == null) return emptyList()
            val moshi = Moshi.Builder().build()

            val dataList = Types.newParameterizedType(List::class.java, Int::class.java)
            val jsonAdapter: JsonAdapter<List<Int>> = moshi.adapter(dataList)

            return jsonAdapter.fromJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun convertOrderListToJson(list: List<Int>?): String {
            val moshi = Moshi.Builder().build()
            val dataList = Types.newParameterizedType(List::class.java, Int::class.java)
            val jsonAdapter: JsonAdapter<List<Int>> = moshi.adapter(dataList)

            return jsonAdapter.toJson(list)
        }
    }
}