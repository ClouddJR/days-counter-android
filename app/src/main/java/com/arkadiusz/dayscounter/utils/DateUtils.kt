package com.arkadiusz.dayscounter.utils

import java.util.*

/**
 * Created by arkadiusz on 02.03.18
 */

object DateUtils {

    fun formatDate(year: Int, month: Int, day: Int): String {
        val formattedMonth = if (month < 10) {
            "0${month + 1}"
        } else {
            "${month + 1}"
        }

        val formattedDay = if (day < 10) {
            "0$day"
        } else {
            "$day"
        }

        return "$year-$formattedMonth-$formattedDay"
    }

    fun formatTime(hour: Int, minute: Int): String {
        val formattedHour = if (hour < 10) {
            "0$hour"
        } else {
            "$hour"
        }

        val formattedMinutes = if (minute < 10) {
            "0$minute"
        } else {
            "$minute"
        }

        return "$formattedHour:$formattedMinutes"
    }


    fun calculateDate(dateYear: Int, dateMonth: Int, dateDay: Int, areYearsIncluded: Boolean,
                      areMonthsIncluded: Boolean, areWeeksIncluded: Boolean, areDaysIncluded: Boolean): String {

        var eventCalendar = generateCalendar(dateYear, dateMonth, dateDay)
        var todayCalendar = generateTodayCalendar()

        var yearsNumber = 0
        var monthsNumber = 0
        var weeksNumber = 0
        var daysNumber = 0

        if (!eventCalendar.before(todayCalendar)) {
            val tempCalendar = eventCalendar
            eventCalendar = todayCalendar
            todayCalendar = tempCalendar
        }

        //calculate years
        if (areYearsIncluded) {
            while (eventCalendar.before(todayCalendar)) {
                eventCalendar.add(Calendar.YEAR, 1)
                yearsNumber++
            }
            if (!eventCalendar.isTheSameInstanceAs(todayCalendar)) {
                eventCalendar.add(Calendar.YEAR, -1)
                yearsNumber--
            }
        }

        //calculate months
        if (areMonthsIncluded) {
            while (eventCalendar.before(todayCalendar)) {
                eventCalendar.add(Calendar.MONTH, 1)
                monthsNumber++
            }
            if (!eventCalendar.isTheSameInstanceAs(todayCalendar)) {
                eventCalendar.add(Calendar.MONTH, -1)
                monthsNumber--
            }
        }

        //calculate weeks
        if (areWeeksIncluded) {
            while (eventCalendar.before(todayCalendar)) {
                eventCalendar.add(Calendar.WEEK_OF_YEAR, 1)
                weeksNumber++
            }
            if (!eventCalendar.isTheSameInstanceAs(todayCalendar)) {
                eventCalendar.add(Calendar.WEEK_OF_YEAR, -1)
                weeksNumber--
            }
        }

        //calculate days
        if (areDaysIncluded) {
            while (eventCalendar.before(todayCalendar)) {
                eventCalendar.add(Calendar.DAY_OF_MONTH, 1)
                daysNumber++
            }
            if (!eventCalendar.isTheSameInstanceAs(todayCalendar)) {
                eventCalendar.add(Calendar.DAY_OF_MONTH, -1)
                daysNumber--
            }
        }

        return "$yearsNumber,$monthsNumber,$weeksNumber,$daysNumber"
    }

    private fun generateCalendar(dateYear: Int, dateMonth: Int, dateDay: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, dateYear)
        calendar.set(Calendar.MONTH, dateMonth - 1)
        calendar.set(Calendar.DAY_OF_MONTH, dateDay)
        return calendar
    }

    private fun generateTodayCalendar(): Calendar {
        return Calendar.getInstance()
    }

    private fun Calendar.isTheSameInstanceAs(calendar: Calendar): Boolean {
        return (this.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                this.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                this.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
    }


}