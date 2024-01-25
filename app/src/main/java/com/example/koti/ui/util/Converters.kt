package com.example.koti.ui.util

import androidx.room.TypeConverter


class Converters {

    @TypeConverter
    fun fromListStringToString(strList: List<String>?): String = strList.toString()

    @TypeConverter
    fun toListStringFromString(stringList: String?): List<String> {
        val result = ArrayList<String>()
        val split = stringList?.replace("[", "")?.replace("]", "")?.replace(" ", "")?.split(",")
        for (n in split!!) {
            try {
                result.add(n)
            } catch (e: Exception) {

            }
        }
        return result
    }


    @TypeConverter
    fun fromListIntToString(intList: List<Int>?): String = intList.toString()

    @TypeConverter
    fun toListIntFromString(stringList: String?): List<Int> {
        val result = ArrayList<Int>()
        val split = stringList?.replace("[", "")?.replace("]", "")?.replace(" ", "")?.split(",")
        for (n in split!!) {
            try {
                result.add(n.toInt())
            } catch (e: Exception) {

            }
        }
        return result
    }
}