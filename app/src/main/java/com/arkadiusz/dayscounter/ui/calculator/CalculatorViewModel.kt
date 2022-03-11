package com.arkadiusz.dayscounter.ui.calculator

import android.content.SharedPreferences
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arkadiusz.dayscounter.data.model.DateComponents
import com.arkadiusz.dayscounter.util.DateUtils
import com.arkadiusz.dayscounter.util.DateUtils.formatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    val calculatedComponents = MutableLiveData<DateComponents>()
    val additionalFormatsCalculatedComponents =
        MutableLiveData<List<Pair<CalculatorComponentsHolder, DateComponents>>>()

    val showStartDatePicker = MutableLiveData<DateComponents>()
    val showEndDatePicker = MutableLiveData<DateComponents>()

    val chosenStartDate = MutableLiveData<String>()
    val chosenEndDate = MutableLiveData<String>()

    val formNotValid = MutableLiveData<Boolean>()

    private var isStartDateSelected = false
    private var chosenStartYear = 0
    private var chosenStartMonth = 0
    private var chosenStartDay = 0

    private var isEndDateSelected = false
    private var chosenEndYear = 0
    private var chosenEndMonth = 0
    private var chosenEndDay = 0


    fun startDateEditTextClicked() {
        showStartDatePicker.value = DateComponents(
            years = chosenStartYear,
            months = chosenStartMonth,
            days = chosenStartDay
        )
    }

    fun dateForStartDateChosen(dateComponents: DateComponents) {
        chosenStartYear = dateComponents.years
        chosenStartMonth = dateComponents.months
        chosenStartDay = dateComponents.days

        val dateString = formatDate(chosenStartYear, chosenStartMonth, chosenStartDay)
        chosenStartDate.value = DateUtils.formatDateAccordingToSettings(
            dateString,
            sharedPreferences["dateFormat"] ?: ""
        )
        isStartDateSelected = true
    }

    fun endDateEditTextClicked() {
        showEndDatePicker.value = DateComponents(
            years = chosenEndYear,
            months = chosenEndMonth,
            days = chosenEndDay
        )
    }

    fun dateForEndDateChosen(dateComponents: DateComponents) {
        chosenEndYear = dateComponents.years
        chosenEndMonth = dateComponents.months
        chosenEndDay = dateComponents.days

        val dateString = formatDate(chosenEndYear, chosenEndMonth, chosenEndDay)
        chosenEndDate.value = DateUtils.formatDateAccordingToSettings(
            dateString,
            sharedPreferences["dateFormat"] ?: ""
        )
        isEndDateSelected = true
    }

    fun calculate(componentsHolder: CalculatorComponentsHolder) {
        if (isStartDateSelected && isEndDateSelected) {
            calculateWithUserSpecifiedComponents(componentsHolder)
            calculateWithAdditionalFormats(componentsHolder)
        } else {
            formNotValid.value = true
        }
    }

    private fun calculateWithUserSpecifiedComponents(componentsHolder: CalculatorComponentsHolder) {
        val startDateCalendar = DateUtils.generateCalendar(
            chosenStartYear,
            chosenStartMonth + 1,
            chosenStartDay
        )

        val components = DateUtils.calculateDate(
            chosenEndYear,
            chosenEndMonth + 1,
            chosenEndDay,
            componentsHolder.areYearsIncluded,
            componentsHolder.areMonthsIncluded,
            componentsHolder.areWeeksIncluded,
            componentsHolder.areDaysIncluded,
            startDateCalendar
        )

        calculatedComponents.value = components
    }

    private fun calculateWithAdditionalFormats(excludedComponentsHolder: CalculatorComponentsHolder) {
        val combinations = generatePossibleCombinationsWith(excludedComponentsHolder)

        val additionalFormatsList = mutableListOf<Pair<CalculatorComponentsHolder,
                DateComponents>>()

        addFormatForEachCombination(combinations, additionalFormatsList)
        addWorkdaysFormat(additionalFormatsList)

        additionalFormatsCalculatedComponents.value = additionalFormatsList
    }

    private fun generatePossibleCombinationsWith(
        excludedComponentsHolder: CalculatorComponentsHolder
    ): MutableList<IntArray> {
        val combinations = mutableListOf<IntArray>()
        for (i in 0..1) {
            for (j in 0..1) {
                for (k in 0..1) {
                    for (l in 0..1) {
                        //continue if all of them are 0
                        if ((i or j or k or l) == 0) continue
                        //also continue when the combination was performed in the first step
                        if (excludedComponentsHolder.areYearsIncluded == (i == 1) &&
                            excludedComponentsHolder.areMonthsIncluded == (j == 1) &&
                            excludedComponentsHolder.areWeeksIncluded == (k == 1) &&
                            excludedComponentsHolder.areDaysIncluded == (l == 1)
                        ) continue
                        combinations.add(intArrayOf(i, j, k, l))
                    }
                }
            }
        }
        return combinations
    }

    private fun addFormatForEachCombination(
        combinations: MutableList<IntArray>,
        additionalFormatsList:
        MutableList<Pair<CalculatorComponentsHolder,
                DateComponents>>
    ) {
        for (combination in combinations) {
            val startDateCalendar = DateUtils.generateCalendar(
                chosenStartYear,
                chosenStartMonth + 1,
                chosenStartDay
            )

            val componentsHolder = CalculatorComponentsHolder(
                areYearsIncluded = combination[0] == 1,
                areMonthsIncluded = combination[1] == 1,
                areWeeksIncluded = combination[2] == 1,
                areDaysIncluded = combination[3] == 1
            )
            val calculatedComponents = DateUtils.calculateDate(
                chosenEndYear,
                chosenEndMonth + 1,
                chosenEndDay,
                componentsHolder.areYearsIncluded,
                componentsHolder.areMonthsIncluded,
                componentsHolder.areWeeksIncluded,
                componentsHolder.areDaysIncluded,
                startDateCalendar
            )

            if (includedFormatPartIsZero(componentsHolder, calculatedComponents)) continue
            if (calculatedCalendarRepresentsTheEndDate(startDateCalendar))
                additionalFormatsList.add(Pair(componentsHolder, calculatedComponents))
        }
    }

    private fun includedFormatPartIsZero(
        componentsHolder: CalculatorComponentsHolder,
        calculatedComponents: DateComponents
    ): Boolean {
        return componentsHolder.areYearsIncluded && calculatedComponents.years == 0 ||
                componentsHolder.areMonthsIncluded && calculatedComponents.months == 0 ||
                componentsHolder.areWeeksIncluded && calculatedComponents.weeks == 0 ||
                componentsHolder.areDaysIncluded && calculatedComponents.days == 0
    }

    private fun calculatedCalendarRepresentsTheEndDate(startDateCalendar: Calendar): Boolean {
        return startDateCalendar.get(Calendar.YEAR) == chosenEndYear &&
                startDateCalendar.get(Calendar.MONTH) == chosenEndMonth &&
                startDateCalendar.get(Calendar.DAY_OF_MONTH) == chosenEndDay
    }

    private fun addWorkdaysFormat(
        additionalFormatsList:
        MutableList<Pair<CalculatorComponentsHolder, DateComponents>>
    ) {
        val startDateCalendar = DateUtils.generateCalendar(
            chosenStartYear,
            chosenStartMonth + 1,
            chosenStartDay
        )

        val workdaysNumber = DateUtils.calculateWorkdays(
            chosenEndYear,
            chosenEndMonth + 1,
            chosenEndDay,
            startDateCalendar
        )

        val componentsHolder = CalculatorComponentsHolder(onlyWorkDays = true)
        additionalFormatsList.add(Pair(componentsHolder, DateComponents(days = workdaysNumber)))
    }
}

data class CalculatorComponentsHolder(
    var areDaysIncluded: Boolean = false,
    var areMonthsIncluded: Boolean = false,
    var areWeeksIncluded: Boolean = false,
    var areYearsIncluded: Boolean = false,

    var onlyWorkDays: Boolean = false
)