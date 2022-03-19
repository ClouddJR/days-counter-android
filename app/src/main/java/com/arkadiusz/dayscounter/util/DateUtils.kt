package com.arkadiusz.dayscounter.util

import android.content.res.Resources
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.DateComponents
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatDate(year: Int, month: Int, day: Int): String {
        if (year < 1 || month < 0 || day < 0) throw IllegalArgumentException()

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
        if (hour < 0 || minute < 0) throw IllegalArgumentException()

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

    fun getDateForBackupFile(calendar: Calendar = Calendar.getInstance()): String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return "$year$month${day}_$hour$minute"
    }

    fun calculateDate(
        passedDate: String, areYearsIncluded: Boolean,
        areMonthsIncluded: Boolean, areWeeksIncluded: Boolean,
        areDaysIncluded: Boolean, resources: Resources
    ): String {

        val triple = getElementsFromDate(passedDate)
        val year = triple.first
        val month = triple.second
        val day = triple.third

        return calculateDate(
            year, month, day, areYearsIncluded, areMonthsIncluded,
            areWeeksIncluded, areDaysIncluded, resources
        )

    }

    fun getElementsFromDate(date: String): Triple<Int, Int, Int> {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date)

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

    fun formatDateAccordingToSettings(
        originalDate: String, datePreference: String,
        locale: Locale = Locale.getDefault()
    ): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(originalDate)

        val formatter = when (datePreference) {
            "31-10-1980" -> SimpleDateFormat("dd-MM-yyyy", locale)
            "10-31-1980" -> SimpleDateFormat("MM-dd-yyyy", locale)
            "1980-10-31" -> SimpleDateFormat("yyyy-MM-dd", locale)
            "31 October 1980" -> SimpleDateFormat("d MMMM yyyy", locale)
            "October 31, 1980" -> SimpleDateFormat("LLLL d, yyyy", locale)
            else -> SimpleDateFormat("yyyy-MM-dd", locale)
        }

        return formatter.format(date)
    }


    fun calculateDate(
        dateYear: Int, dateMonth: Int, dateDay: Int, areYearsIncluded: Boolean,
        areMonthsIncluded: Boolean, areWeeksIncluded: Boolean,
        areDaysIncluded: Boolean, resources: Resources,
        today: Calendar = generateTodayCalendar()
    ): String {

        var eventCalendar = generateCalendar(dateYear, dateMonth, dateDay)
        var todayCalendar = today

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

        return generateCounterText(
            yearsNumber, monthsNumber, weeksNumber, daysNumber,
            areYearsIncluded, areMonthsIncluded, areWeeksIncluded, areDaysIncluded,
            resources
        )
    }

    fun calculateDate(
        dateYear: Int, dateMonth: Int, dateDay: Int, areYearsIncluded: Boolean,
        areMonthsIncluded: Boolean, areWeeksIncluded: Boolean,
        areDaysIncluded: Boolean,
        today: Calendar = generateTodayCalendar()
    ): DateComponents {

        var eventCalendar = generateCalendar(dateYear, dateMonth, dateDay)
        var todayCalendar = today

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

        return DateComponents(
            yearsNumber,
            monthsNumber,
            weeksNumber,
            daysNumber
        )
    }

    fun generateCounterText(
        yearsNumber: Int, monthsNumber: Int, weeksNumber: Int,
        daysNumber: Int,
        areYearsIncluded: Boolean, areMonthsIncluded: Boolean,
        areWeeksIncluded: Boolean, areDaysIncluded: Boolean,
        resources: Resources
    ): String {
        if (yearsNumber == 0 && monthsNumber == 0 && weeksNumber == 0 && daysNumber == 0) {
            return resources.getString(R.string.date_utils_today)
        }

        var counterText = ""
        if (areYearsIncluded) counterText += "$yearsNumber " +
                "${resources.getQuantityString(R.plurals.years_number, yearsNumber)} "

        if (areMonthsIncluded) counterText += "$monthsNumber " +
                "${resources.getQuantityString(R.plurals.months_number, monthsNumber)} "

        if (areWeeksIncluded) counterText += "$weeksNumber " +
                "${resources.getQuantityString(R.plurals.weeks_number, weeksNumber)} "

        if (areDaysIncluded) counterText += "$daysNumber " +
                "${resources.getQuantityString(R.plurals.days_number, daysNumber)} "

        return counterText.trim()
    }

    fun calculateWorkdays(
        dateYear: Int, dateMonth: Int, dateDay: Int,
        today: Calendar = generateTodayCalendar()
    ): Int {

        var eventCalendar = generateCalendar(dateYear, dateMonth, dateDay)
        var todayCalendar = today

        if (!eventCalendar.before(todayCalendar)) {
            val tempCalendar = eventCalendar
            eventCalendar = todayCalendar
            todayCalendar = tempCalendar
        }

        var daysNumber = 0
        while (eventCalendar.before(todayCalendar)) {
            val dayOfWeek = eventCalendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                daysNumber++
            }
            eventCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        if (!eventCalendar.isTheSameInstanceAs(todayCalendar)) {
            eventCalendar.add(Calendar.DAY_OF_MONTH, -1)
            daysNumber--
        }
        return daysNumber
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