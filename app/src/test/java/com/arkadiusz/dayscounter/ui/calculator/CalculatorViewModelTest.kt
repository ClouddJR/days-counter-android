package com.arkadiusz.dayscounter.ui.calculator

import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arkadiusz.dayscounter.data.model.DateComponents
import com.arkadiusz.dayscounter.utils.DateUtils
import com.arkadiusz.dayscounter.utils.DateUtils.formatDate
import com.arkadiusz.dayscounter.utils.DateUtils.formatDateAccordingToSettings
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import java.util.*

class CalculatorViewModelTest {

    private lateinit var calculatorViewModel: CalculatorViewModel

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        sharedPreferences = Mockito.mock(SharedPreferences::class.java)
        calculatorViewModel = CalculatorViewModel(sharedPreferences)
    }

    @Test
    fun `should not invoke calculation when start date is not defined`() {
        //given the start date unset
        calculatorViewModel.dateForEndDateChosen(DateComponents(2020, 0, 0, 1))

        //when invoking calculation
        calculatorViewModel.calculate(CalculatorComponentsHolder())

        //then the corresponding live data object should be updated
        assertTrue(calculatorViewModel.formNotValid.value ?: false)
    }

    @Test
    fun `should not invoke calculation when end date is not defined`() {
        //given the end date unset
        calculatorViewModel.dateForStartDateChosen(DateComponents(2020, 0, 0, 1))

        //when invoking calculation
        calculatorViewModel.calculate(CalculatorComponentsHolder())

        //then the corresponding live data object should be updated
        assertTrue(calculatorViewModel.formNotValid.value ?: false)
    }

    @Test
    fun `should not invoke calculation when both dates are not defined`() {
        //given the start date and end date both unset

        //when invoking calculation
        calculatorViewModel.calculate(CalculatorComponentsHolder())

        //then the corresponding live data object should be updated
        assertTrue(calculatorViewModel.formNotValid.value ?: false)
    }

    @Test
    fun `should update live data object when calculation is invoked`() {
        //given components representing dates different by 4 days
        calculatorViewModel.dateForStartDateChosen(DateComponents(
                years = 2020,
                months = 0,
                days = 1
        ))
        calculatorViewModel.dateForEndDateChosen(DateComponents(
                years = 2020,
                months = 0,
                days = 5
        ))
        val calculatorComponentsHolder = CalculatorComponentsHolder(
                areDaysIncluded = true,
                areMonthsIncluded = false,
                areWeeksIncluded = false,
                areYearsIncluded = false,
                onlyWorkDays = false
        )

        //when calculating the resulting components
        calculatorViewModel.calculate(calculatorComponentsHolder)

        //it should update corresponding the live data object
        val correctlyCalculatedComponents = DateComponents(0, 0, 0, 4)
        assertEquals(correctlyCalculatedComponents, calculatorViewModel.calculatedComponents.value)
    }

    @Test
    fun `should add correct additional formats after calculating the main result`() {
        //given components representing dates different by 20 days (and 14 workdays)
        calculatorViewModel.dateForStartDateChosen(DateComponents(
                years = 2020,
                months = 2,
                days = 6
        ))
        calculatorViewModel.dateForEndDateChosen(DateComponents(
                years = 2020,
                months = 2,
                days = 26
        ))
        val calculatorComponentsHolder = CalculatorComponentsHolder(
                areDaysIncluded = true,
                areMonthsIncluded = false,
                areWeeksIncluded = false,
                areYearsIncluded = false,
                onlyWorkDays = false
        )

        //when calculating the resulting components
        calculatorViewModel.calculate(calculatorComponentsHolder)

        //it should update the corresponding additional formats live data object
        val additionalFormats = mutableListOf(
                Pair(CalculatorComponentsHolder(areWeeksIncluded = true, areDaysIncluded = true),
                        DateComponents(0, 0, 2, 6)),
                Pair(CalculatorComponentsHolder(onlyWorkDays = true), DateComponents(-1, -1, -1, 14))
        )
        assertEquals(additionalFormats,
                calculatorViewModel.additionalFormatsCalculatedComponents.value)
    }

    @Test
    fun `should update the start date components when choosing the start date`() {
        //given the date components
        val dateComponents = DateComponents(
                years = 2020,
                months = 0,
                days = 1
        )

        //when the starting date is chosen
        calculatorViewModel.dateForStartDateChosen(dateComponents)

        //then the corresponding live data object and boolean variable should be the updated
        val dateString = formatDate(dateComponents.years, dateComponents.months, dateComponents.days)
        val formattedDate = formatDateAccordingToSettings(dateString, "", Locale.US)
        assertEquals(calculatorViewModel.chosenStartDate.value, formattedDate)
    }

    @Test
    fun `should update the end date components when choosing the end date`() {
        //given the date components
        val dateComponents = DateComponents(
                years = 2020,
                months = 0,
                days = 1
        )

        //when the ending date is chosen
        calculatorViewModel.dateForEndDateChosen(dateComponents)

        //then the corresponding live data object should be the updated
        val dateString = formatDate(dateComponents.years, dateComponents.months, dateComponents.days)
        val formattedDate = formatDateAccordingToSettings(dateString, "", Locale.US)
        assertEquals(calculatorViewModel.chosenEndDate.value, formattedDate)
    }

    @Test
    fun `should return previous start date components when edit text was clicked again`() {
        //given the previous date components
        val dateComponents = DateComponents(
                years = 2020,
                months = 0,
                days = 1
        )

        //when the start date was chosen and the edit text is clicked again
        calculatorViewModel.dateForStartDateChosen(dateComponents)
        calculatorViewModel.startDateEditTextClicked()

        //then it should return the previous date components
        assertEquals(calculatorViewModel.showStartDatePicker.value, dateComponents)
    }

    @Test
    fun `should return zeroed start date components when edit text was clicked for the first time`() {
        //given the date components
        val dateComponents = DateComponents(
                years = 0,
                months = 0,
                days = 0
        )

        //when the edit text is clicked
        calculatorViewModel.startDateEditTextClicked()

        //then it should return zeroed date components
        assertEquals(calculatorViewModel.showStartDatePicker.value, dateComponents)
    }

    @Test
    fun `should return previous end date components when edit text was clicked again`() {
        //given the previous date components
        val dateComponents = DateComponents(
                years = 2020,
                months = 0,
                days = 5
        )

        //when the end end date was chosen and the edit text is clicked again
        calculatorViewModel.dateForEndDateChosen(dateComponents)
        calculatorViewModel.endDateEditTextClicked()

        //then it should return the previous date components
        assertEquals(calculatorViewModel.showEndDatePicker.value, dateComponents)
    }

    @Test
    fun `should return zeroed end date components when edit text was clicked for the first time`() {
        //given the date components
        val dateComponents = DateComponents(
                years = 0,
                months = 0,
                days = 0
        )

        //when the edit text is clicked
        calculatorViewModel.endDateEditTextClicked()

        //then it should return the zeroed date components
        assertEquals(calculatorViewModel.showEndDatePicker.value, dateComponents)
    }
}