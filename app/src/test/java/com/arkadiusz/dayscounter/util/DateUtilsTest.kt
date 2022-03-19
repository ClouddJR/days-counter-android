package com.arkadiusz.dayscounter.util

import android.content.res.Resources
import com.arkadiusz.dayscounter.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.text.ParseException
import java.util.*

class DateUtilsTest {

    @Test
    fun `should return correctly formatted date`() {
        val year = 2020
        val monthLessThan10 = 5
        val monthMoreThan10 = 11
        val dayLessThan10 = 9
        val dayMoreThan10 = 20

        assertEquals("2020-06-09", DateUtils.formatDate(year, monthLessThan10, dayLessThan10))
        assertEquals("2020-12-20", DateUtils.formatDate(year, monthMoreThan10, dayMoreThan10))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw an exception when formatting date and passing wrong year argument`() {
        val wrongYear = 0
        val month = 10
        val day = 20

        DateUtils.formatDate(wrongYear, month, day)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw an exception when formatting date and passing wrong month argument`() {
        val year = 2020
        val wrongMonth = -1
        val day = 20

        DateUtils.formatDate(year, wrongMonth, day)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw an exception when formatting date and passing wrong day argument`() {
        val year = 2020
        val month = -1
        val wrongDay = 20

        DateUtils.formatDate(year, month, wrongDay)
    }

    @Test
    fun `should return correctly formatted time`() {
        val hourLessThan10 = 5
        val hourMoreThan10 = 11
        val minuteLessThan10 = 9
        val minuteMoreThan10 = 20

        assertEquals("05:09", DateUtils.formatTime(hourLessThan10, minuteLessThan10))
        assertEquals("11:20", DateUtils.formatTime(hourMoreThan10, minuteMoreThan10))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw an exception when formatting time and passing wrong hour argument`() {
        val wrongHour = -1
        val minute = 10

        DateUtils.formatTime(wrongHour, minute)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw an exception when formatting time and passing wrong minute argument`() {
        val hour = 10
        val wrongMinute = -1

        DateUtils.formatTime(hour, wrongMinute)
    }

    @Test
    fun `should return correctly formatted date for backup`() {
        val year = 2020
        val month = 5
        val day = 20

        val hour = 10
        val minute = 15

        val calendar = mock(Calendar::class.java)
        `when`(calendar.get(Calendar.YEAR)).thenReturn(year)
        `when`(calendar.get(Calendar.MONTH)).thenReturn(month)
        `when`(calendar.get(Calendar.DAY_OF_MONTH)).thenReturn(day)
        `when`(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(hour)
        `when`(calendar.get(Calendar.MINUTE)).thenReturn(minute)

        assertEquals(
            "$year${month + 1}${day}_$hour$minute",
            DateUtils.getDateForBackupFile(calendar)
        )
    }

    @Test
    fun `should return correct date elements from string when date has month lesser than 10`() {
        val dateWithMonthLesserThan10 = "2020-05-10"

        val elements = DateUtils.getElementsFromDate(dateWithMonthLesserThan10)
        assertEquals(elements.first, 2020)
        assertEquals(elements.second, 5)
        assertEquals(elements.third, 10)
    }

    @Test
    fun `should return correct date elements from string when date has days lesser than 10`() {
        val dateWithDayLesserThan10 = "2020-05-09"

        val elements = DateUtils.getElementsFromDate(dateWithDayLesserThan10)
        assertEquals(elements.first, 2020)
        assertEquals(elements.second, 5)
        assertEquals(elements.third, 9)
    }

    @Test
    fun `should return correct date elements from string when date has month bigger than 10`() {
        val dateWithMonthBiggerThan10 = "2020-11-10"

        val elements = DateUtils.getElementsFromDate(dateWithMonthBiggerThan10)
        assertEquals(elements.first, 2020)
        assertEquals(elements.second, 11)
        assertEquals(elements.third, 10)
    }

    @Test
    fun `should return correct date elements from string when date has days bigger than 10`() {
        val dateWithDayBiggerThan10 = "2020-05-21"

        val elements = DateUtils.getElementsFromDate(dateWithDayBiggerThan10)
        assertEquals(elements.first, 2020)
        assertEquals(elements.second, 5)
        assertEquals(elements.third, 21)
    }

    @Test(expected = ParseException::class)
    fun `should throw an exception when getting elements from a date and passing wrong date`() {
        val wrongDate = "2020-10.10"
        DateUtils.getElementsFromDate(wrongDate)
    }

    @Test
    fun `should return correctly formatted date according to settings when preference is dd-MM-YYYY`() {
        val originalDate = "2020-01-15"

        val datePreference = "31-10-1980"
        assertEquals(
            "15-01-2020", DateUtils.formatDateAccordingToSettings(
                originalDate,
                datePreference, Locale.US
            )
        )
    }

    @Test
    fun `should return correctly formatted date according to settings when preference is MM-dd-YYYY`() {
        val originalDate = "2020-01-15"

        val datePreference = "10-31-1980"
        assertEquals(
            "01-15-2020", DateUtils.formatDateAccordingToSettings(
                originalDate,
                datePreference, Locale.US
            )
        )
    }

    @Test
    fun `should return correctly formatted date according to settings when preference is yyyy-MM-dd`() {
        val originalDate = "2020-01-15"

        val datePreference = "1980-10-31"
        assertEquals(
            "2020-01-15", DateUtils.formatDateAccordingToSettings(
                originalDate,
                datePreference, Locale.US
            )
        )
    }

    @Test
    fun `should return correctly formatted date according to settings when preference is d MMMM yyyy`() {
        val originalDate = "2020-01-15"

        val datePreference = "31 October 1980"
        assertEquals(
            "15 January 2020", DateUtils.formatDateAccordingToSettings(
                originalDate,
                datePreference, Locale.US
            )
        )
    }

    @Test
    fun `should return correctly formatted date according to settings when preference is LLLL d, yyyy`() {
        val originalDate = "2020-01-15"

        val datePreference = "October 31, 1980"
        assertEquals(
            "January 15, 2020", DateUtils.formatDateAccordingToSettings(
                originalDate,
                datePreference, Locale.US
            )
        )
    }

    @Test
    fun `test calculate only years when year is the same in both dates`() {
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2020)
            set(Calendar.MONTH, 3)
            set(Calendar.DAY_OF_MONTH, 10)
        }
        val year = 2020
        val month = 6
        val day = 10

        assertEquals(
            0, DateUtils.calculateDate(
                year, month, day,
                areYearsIncluded = true,
                areMonthsIncluded = false,
                areWeeksIncluded = false,
                areDaysIncluded = false,
                today = todayCalendar
            ).years
        )
    }

    @Test
    fun `test calculate only years when years are different in both dates`() {
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, 6)
            set(Calendar.DAY_OF_MONTH, 10)
        }
        val year = 2020
        val month = 6
        val day = 10

        assertEquals(
            4, DateUtils.calculateDate(
                year, month, day,
                areYearsIncluded = true,
                areMonthsIncluded = false,
                areWeeksIncluded = false,
                areDaysIncluded = false,
                today = todayCalendar
            ).years
        )
    }

    @Test
    fun `test calculate only months when months is the same in both dates`() {
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2020)
            set(Calendar.MONTH, 5)
            set(Calendar.DAY_OF_MONTH, 10)
        }
        val year = 2020
        val month = 6
        val day = 10

        assertEquals(
            0, DateUtils.calculateDate(
                year, month, day,
                areYearsIncluded = false,
                areMonthsIncluded = true,
                areWeeksIncluded = false,
                areDaysIncluded = false,
                today = todayCalendar
            ).months
        )
    }

    @Test
    fun `test calculate only months when months are different in both dates`() {
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2020)
            set(Calendar.MONTH, 6)
            set(Calendar.DAY_OF_MONTH, 10)
        }
        val year = 2021
        val month = 2
        val day = 10

        assertEquals(
            7, DateUtils.calculateDate(
                year, month, day,
                areYearsIncluded = false,
                areMonthsIncluded = true,
                areWeeksIncluded = false,
                areDaysIncluded = false,
                today = todayCalendar
            ).months
        )
    }

    @Test
    fun `test calculate only weeks when weeks are the same in both dates`() {
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2020)
            set(Calendar.MONTH, 6)
            set(Calendar.DAY_OF_MONTH, 11)
        }
        val year = 2020
        val month = 7
        val day = 14

        assertEquals(
            0, DateUtils.calculateDate(
                year, month, day,
                areYearsIncluded = false,
                areMonthsIncluded = false,
                areWeeksIncluded = true,
                areDaysIncluded = false,
                today = todayCalendar
            ).weeks
        )
    }

    @Test
    fun `test calculate only weeks when weeks are different in both dates`() {
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2020)
            set(Calendar.MONTH, 6)
            set(Calendar.DAY_OF_MONTH, 11)
        }
        val year = 2020
        val month = 8
        val day = 14

        assertEquals(
            4, DateUtils.calculateDate(
                year, month, day,
                areYearsIncluded = false,
                areMonthsIncluded = false,
                areWeeksIncluded = true,
                areDaysIncluded = false,
                today = todayCalendar
            ).weeks
        )
    }

    @Test
    fun `test calculate only days when days are the same in both dates`() {
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2020)
            set(Calendar.MONTH, 6)
            set(Calendar.DAY_OF_MONTH, 11)
        }
        val year = 2020
        val month = 7
        val day = 11

        assertEquals(
            0, DateUtils.calculateDate(
                year, month, day,
                areYearsIncluded = false,
                areMonthsIncluded = false,
                areWeeksIncluded = false,
                areDaysIncluded = true,
                today = todayCalendar
            ).days
        )
    }

    @Test
    fun `test calculate only days when days are different in both dates`() {
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2020)
            set(Calendar.MONTH, 6)
            set(Calendar.DAY_OF_MONTH, 11)
        }
        val year = 2020
        val month = 8
        val day = 14

        assertEquals(
            34, DateUtils.calculateDate(
                year, month, day,
                areYearsIncluded = false,
                areMonthsIncluded = false,
                areWeeksIncluded = false,
                areDaysIncluded = true,
                today = todayCalendar
            ).days
        )
    }

    @Test
    fun `test calculate with multiple variations`() {
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2020)
            set(Calendar.MONTH, 6)
            set(Calendar.DAY_OF_MONTH, 11)
        }
        val year = 2023
        val month = 9
        val day = 21

        val calculatedComponents = DateUtils.calculateDate(
            year, month, day,
            areYearsIncluded = true,
            areMonthsIncluded = true,
            areWeeksIncluded = true,
            areDaysIncluded = true,
            today = todayCalendar
        )

        assertEquals(3, calculatedComponents.years)
        assertEquals(2, calculatedComponents.months)
        assertEquals(1, calculatedComponents.weeks)
        assertEquals(3, calculatedComponents.days)
    }

    @Test
    fun `should return correctly formatted years number text`() {
        val resources = mock(Resources::class.java)

        `when`(
            resources.getQuantityString(
                eq(R.plurals.years_number),
                any(Int::class.java)
            )
        )
            .thenReturn("years")
        `when`(resources.getQuantityString(R.plurals.years_number, 1)).thenReturn("year")

        var yearsNumber = 2
        assertEquals(
            "2 years", DateUtils.generateCounterText(
                yearsNumber, 0, 0, 0,
                areYearsIncluded = true,
                areMonthsIncluded = false,
                areWeeksIncluded = false,
                areDaysIncluded = false,
                resources = resources
            )
        )

        yearsNumber = 1
        assertEquals(
            "1 year", DateUtils.generateCounterText(
                yearsNumber, 0, 0, 0,
                areYearsIncluded = true,
                areMonthsIncluded = false,
                areWeeksIncluded = false,
                areDaysIncluded = false,
                resources = resources
            )
        )
    }

    @Test
    fun `should return correctly formatted months number text`() {
        val resources = mock(Resources::class.java)

        `when`(
            resources.getQuantityString(
                eq(R.plurals.months_number),
                any(Int::class.java)
            )
        )
            .thenReturn("months")
        `when`(resources.getQuantityString(R.plurals.months_number, 1)).thenReturn("month")

        var monthsNumber = 2
        assertEquals(
            "2 months", DateUtils.generateCounterText(
                0, monthsNumber, 0, 0,
                areYearsIncluded = false,
                areMonthsIncluded = true,
                areWeeksIncluded = false,
                areDaysIncluded = false,
                resources = resources
            )
        )

        monthsNumber = 1
        assertEquals(
            "1 month", DateUtils.generateCounterText(
                0, monthsNumber, 0, 0,
                areYearsIncluded = false,
                areMonthsIncluded = true,
                areWeeksIncluded = false,
                areDaysIncluded = false,
                resources = resources
            )
        )
    }


    @Test
    fun `should return correctly formatted weeks number text`() {
        val resources = mock(Resources::class.java)

        `when`(
            resources.getQuantityString(
                eq(R.plurals.weeks_number),
                any(Int::class.java)
            )
        )
            .thenReturn("weeks")
        `when`(resources.getQuantityString(R.plurals.weeks_number, 1)).thenReturn("week")

        var weeksNumber = 2
        assertEquals(
            "2 weeks", DateUtils.generateCounterText(
                0, 0, weeksNumber, 0,
                areYearsIncluded = false,
                areMonthsIncluded = false,
                areWeeksIncluded = true,
                areDaysIncluded = false,
                resources = resources
            )
        )

        weeksNumber = 1
        assertEquals(
            "1 week", DateUtils.generateCounterText(
                0, 0, weeksNumber, 0,
                areYearsIncluded = false,
                areMonthsIncluded = false,
                areWeeksIncluded = true,
                areDaysIncluded = false,
                resources = resources
            )
        )
    }

    @Test
    fun `should return correctly formatted days number text`() {
        val resources = mock(Resources::class.java)

        `when`(resources.getQuantityString(eq(R.plurals.days_number), any(Int::class.java)))
            .thenReturn("days")
        `when`(resources.getQuantityString(R.plurals.days_number, 1)).thenReturn("day")

        var daysNumber = 2
        assertEquals(
            "2 days", DateUtils.generateCounterText(
                0, 0, 0, daysNumber,
                areYearsIncluded = false,
                areMonthsIncluded = false,
                areWeeksIncluded = false,
                areDaysIncluded = true,
                resources = resources
            )
        )

        daysNumber = 1
        assertEquals(
            "1 day", DateUtils.generateCounterText(
                0, 0, 0, daysNumber,
                areYearsIncluded = false,
                areMonthsIncluded = false,
                areWeeksIncluded = false,
                areDaysIncluded = true,
                resources = resources
            )
        )
    }

    @Test
    fun `should return today word when dates are the same`() {
        val resources = mock(Resources::class.java)
        `when`(resources.getString(R.string.date_utils_today)).thenReturn("Today!")
        assertEquals(
            "Today!", DateUtils.generateCounterText(
                0, 0, 0, 0,
                areYearsIncluded = false,
                areMonthsIncluded = false,
                areWeeksIncluded = false,
                areDaysIncluded = true,
                resources = resources
            )
        )
    }

    @Test
    fun `should return correct calendar`() {
        val calendarYear = 2020
        val calendarMonth = 5
        val calendarDay = 3

        val calendar = DateUtils.generateCalendar(calendarYear, calendarMonth, calendarDay)
        assertEquals(calendarYear, calendar.get(Calendar.YEAR))
        assertEquals(calendarMonth - 1, calendar.get(Calendar.MONTH))
        assertEquals(calendarDay, calendar.get(Calendar.DAY_OF_MONTH))
    }
}