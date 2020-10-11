package com.sillyapps.meantime

val UNCERTAIN = -2L

fun convertToMillis(hours: Int, minutes: Int): Long {
    return (hours*60L + minutes)*60000L
}