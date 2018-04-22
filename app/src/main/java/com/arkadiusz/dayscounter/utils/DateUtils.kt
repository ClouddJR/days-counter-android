package com.arkadiusz.dayscounter.utils

import android.content.Context
import com.arkadiusz.dayscounter.R
import java.util.*

/**
 * Created by arkadiusz on 02.03.18
 */

object DateUtils {

    fun formatDate(year: Int, month: Int, day: Int): String {
        val formattedMonth = if (month + 1 < 10) {
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

    fun calculateDate(passedDate: String, areYearsIncluded: Boolean,
                      areMonthsIncluded: Boolean, areWeeksIncluded: Boolean, areDaysIncluded: Boolean, context: Context): String {

        val triple = getElementsFromDate(passedDate)
        val year = triple.first
        val month = triple.second
        val day = triple.third

        return calculateDate(year, month, day, areYearsIncluded, areMonthsIncluded, areWeeksIncluded, areDaysIncluded, context)

    }

    fun getElementsFromDate(date: String): Triple<Int, Int, Int> {
        val year = date.substring(0, 4).toInt()
        val month = if (date[5] == '0') {
            date.substring(6, 7).toInt()
        } else {
            date.substring(5, 7).toInt()
        }
        val day = if (date[8] == '0') {
            date.substring(9, 10).toInt()
        } else {
            date.substring(8, 10).toInt()
        }

        return Triple(year, month, day)
    }


    fun calculateDate(dateYear: Int, dateMonth: Int, dateDay: Int, areYearsIncluded: Boolean,
                      areMonthsIncluded: Boolean, areWeeksIncluded: Boolean, areDaysIncluded: Boolean, context: Context): String {

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

        return generateCounterText(yearsNumber, monthsNumber, weeksNumber, daysNumber,
                areYearsIncluded, areMonthsIncluded, areWeeksIncluded, areDaysIncluded,
                context)
    }

    private fun generateCounterText(yearsNumber: Int, monthsNumber: Int, weeksNumber: Int, daysNumber: Int,
                                    areYearsIncluded: Boolean, areMonthsIncluded: Boolean, areWeeksIncluded: Boolean, areDaysIncluded: Boolean,
                                    context: Context): String {
        var counterText = ""
        if (yearsNumber > 1) {
            counterText += if (yearsNumber < 5 ||
                    (yearsNumber % 10 == 2 && yearsNumber != 12) ||
                    (yearsNumber % 10 == 3 && yearsNumber != 13) ||
                    (yearsNumber % 10 == 4 && yearsNumber != 14)) {
                "$yearsNumber " + context.getString(R.string.date_utils_multiple_years_below5) + " "
            } else {
                "$yearsNumber " + context.getString(R.string.date_utils_multiple_years) + " "
            }
        } else if (yearsNumber == 1) {
            counterText += "$yearsNumber " + context.getString(R.string.date_utils_single_year) + " "
        } else if (yearsNumber == 0 && areYearsIncluded) {
            counterText += "0 ${context.getString(R.string.date_utils_multiple_years)} "
        }

        if (monthsNumber > 1) {
            counterText += if (monthsNumber < 5 ||
                    (monthsNumber % 10 == 2 && monthsNumber != 12) ||
                    (monthsNumber % 10 == 3 && monthsNumber != 13) ||
                    (monthsNumber % 10 == 4 && monthsNumber != 14)) {
                "$monthsNumber " + context.getString(R.string.date_utils_multiple_months_below5) + " "
            } else {
                "$monthsNumber " + context.getString(R.string.date_utils_multiple_months) + " "
            }
        } else if (monthsNumber == 1) {
            counterText += "$monthsNumber " + context.getString(R.string.date_utils_single_month) + " "
        } else if (monthsNumber == 0 && areMonthsIncluded) {
            counterText += "0 ${context.getString(R.string.date_utils_multiple_months)} "
        }

        if (weeksNumber > 1) {
            counterText += if (weeksNumber < 5 ||
                    (weeksNumber % 10 == 2 && weeksNumber != 12) ||
                    (weeksNumber % 10 == 3 && weeksNumber != 13) ||
                    (weeksNumber % 10 == 4 && weeksNumber != 14)) {
                "$weeksNumber " + context.getString(R.string.date_utils_multiple_weeks_below5) + " "
            } else {
                "$weeksNumber " + context.getString(R.string.date_utils_multiple_weeks) + " "
            }
        } else if (weeksNumber == 1) {
            counterText += "$weeksNumber " + context.getString(R.string.date_utils_single_week) + " "
        } else if (weeksNumber == 0 && areWeeksIncluded) {
            counterText += "0 ${context.getString(R.string.date_utils_multiple_weeks)} "
        }

        if (daysNumber > 1) {
            counterText += "$daysNumber " + context.getString(R.string.date_utils_multiple_days)
        } else if (daysNumber == 1) {
            counterText += "$daysNumber " + context.getString(R.string.date_utils_single_day)
        } else if (daysNumber == 0 && areDaysIncluded) {
            counterText += "0 ${context.getString(R.string.date_utils_multiple_days)} "
        }

        if (yearsNumber == 0 && monthsNumber == 0 && weeksNumber == 0 && daysNumber == 0) {
            counterText = context.getString(R.string.date_utils_today)
        }
        return counterText.trim()
    }

    fun generateCalendar(dateYear: Int, dateMonth: Int, dateDay: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, dateYear)
        calendar.set(Calendar.MONTH, dateMonth - 1)
        calendar.set(Calendar.DAY_OF_MONTH, dateDay)
        return calendar
    }

    fun generateTodayCalendar(): Calendar {
        return Calendar.getInstance()
    }

    private fun Calendar.isTheSameInstanceAs(calendar: Calendar): Boolean {
        return (this.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                this.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                this.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
    }


}