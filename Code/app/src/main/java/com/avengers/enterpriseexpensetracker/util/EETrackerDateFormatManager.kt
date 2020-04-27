package com.avengers.enterpriseexpensetracker.util

import android.os.Build
import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import java.util.regex.Pattern

class EETrackerDateFormatManager() {

    fun parseDate(command: String): String? {
        try {
            val tokens = command.split(" ")
            var dayIndex = -1
            var monthIndex = -1
            val yearIndex = 2

            // array with 3 strings to hold formats for day, month and year
            val formatter = arrayOfNulls<String>(3)

            // Check which place digit occur
            if (tokens[0].toCharArray()[0].isDigit()) {
                dayIndex = 0
                monthIndex = 1
            } else if (tokens[1].toCharArray()[0].isDigit()) {
                dayIndex = 1
                monthIndex = 0
            }

            if (isOrdinal(tokens[dayIndex])) {
                // ordinal format present
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    formatter[dayIndex] = "d['st']['nd']['rd']['th']"
                } else {
                    return null
                }
            } else {
                // normal format
                formatter[dayIndex] = "d"
            }

            formatter[monthIndex] = "MMMM"
            formatter[yearIndex] = "yyyy"

            // Merge all formats
            val parseFormat = formatter[0] + " " + formatter[1] + " " + formatter[2]

            val parsedCommand = tokens[0] + " " + tokens[1] + " " + tokens[2]

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var dateTimeFormatter = DateTimeFormatter.ofPattern(parseFormat, Locale.ENGLISH)
                dateTimeFormatter =
                    DateTimeFormatterBuilder().parseCaseInsensitive().append(dateTimeFormatter).toFormatter()
                val dateTime = LocalDate.parse(parsedCommand, dateTimeFormatter)
                val converted = DateTimeFormatter.ofPattern("MM/dd/yyyy").format(dateTime)
                Log.d("EETracker *******", "converted date: $converted")
                return converted
            } else {
                // convert to mm/dd/yyyy
                val dateFormatter = SimpleDateFormat(parseFormat, Locale.US)
                val date = dateFormatter.parse(parsedCommand)
                Log.d("EETracker *******", "parsed date: $date")
                val converted = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date!!)
                Log.d("EETracker *******", "converted date: $converted")
                return converted
            }
        } catch (e: ParseException) {
            Log.e("EETracker *******", "Exception: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    private fun isOrdinal(day: String): Boolean {
        val pat = Pattern.compile("\\d+(st|nd|rd|th)")
        val groups = pat.matcher(day)
        return groups.matches()
    }

    fun mapActualMonth(month: Int): String {
        when (month) {
            0 -> return "01"
            1 -> return "02"
            2 -> return "03"
            3 -> return "04"
            4 -> return "05"
            5 -> return "06"
            6 -> return "07"
            7 -> return "08"
            8 -> return "09"
            9 -> return "10"
            10 -> return "11"
            11 -> return "12"
        }

        return "00"
    }

    fun dayFormat(day: Int): String {
        var strDay = day.toString()
        if (strDay.length < 2) {
            strDay = "0$strDay"
        }

        return strDay
    }
}