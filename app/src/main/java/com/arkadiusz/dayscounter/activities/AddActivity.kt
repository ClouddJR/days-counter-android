package com.arkadiusz.dayscounter.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.*
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.adapters.FontTypeSpinnerAdapter
import com.arkadiusz.dayscounter.utils.DateUtils.calculateDate
import com.arkadiusz.dayscounter.utils.DateUtils.formatDate
import com.arkadiusz.dayscounter.utils.DateUtils.formatTime
import com.arkadiusz.dayscounter.utils.DateUtils.generateTodayCalendar
import com.arkadiusz.dayscounter.utils.FontUtils
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.content_add_r.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast
import java.util.*


/**
 * Created by arkadiusz on 04.03.18
 */

class AddActivity : AppCompatActivity() {

    private var chosenYear = 0
    private var chosenMonth = 0
    private var chosenDay = 0

    private var reminderDate = ""
    private var chosenReminderHour = 0
    private var chosenReminderMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_add_r)
        setCurrentDateInForm()
        setUpSpinners()
        setUpCheckboxes()
        setUpEditTexts()
        setUpSeekBar()
        setUpFontPicker()
        setUpOnClickListeners()
    }

    private fun setCurrentDateInForm() {
        val calendar = generateTodayCalendar()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        chosenYear = year
        chosenMonth = month
        chosenDay = day
        dateEditText.setText(formatDate(year, month, day))
        eventCalculateText.text = generateCounterText()
    }

    override fun onBackPressed() {
        alert(R.string.add_activity_back_button_message) {
            positiveButton("OK") {
                super.onBackPressed()
            }
            negativeButton(R.string.add_activity_back_button_cancel) {
                it.dismiss()
            }
        }.show()
    }

    private fun setUpSpinners() {
        val fontSizeAdapter = ArrayAdapter.createFromResource(this, R.array.add_activity_font_size, R.layout.support_simple_spinner_dropdown_item)
        counterFontSizeSpinner.adapter = fontSizeAdapter
        titleFontSizeSpinner.adapter = fontSizeAdapter
        counterFontSizeSpinner.setSelection(5)
        titleFontSizeSpinner.setSelection(4)

        val repetitionAdapter = ArrayAdapter.createFromResource(this, R.array.add_activity_repeat, R.layout.support_simple_spinner_dropdown_item)
        repeatSpinner.adapter = repetitionAdapter
        repeatSpinner.setSelection(0)

        val fontTypeAdapter = FontTypeSpinnerAdapter(this, R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.font_type).toList())
        fontTypeSpinner.adapter = fontTypeAdapter
        fontTypeSpinner.setSelection(8)

        setSpinnersListeners()
    }

    private fun setSpinnersListeners() {
        counterFontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                eventCalculateText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (view as TextView).text.toString().toFloat())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing
            }
        }

        titleFontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                eventTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (view as TextView).text.toString().toFloat())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing
            }
        }
        fontTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val fontName = (view as TextView).text.toString()
                val typeFace = FontUtils.getFontFor(fontName, this@AddActivity)
                eventTitle.typeface = typeFace
                eventCalculateText.typeface = typeFace
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing
            }
        }
    }

    private fun setUpCheckboxes() {
        showDividerCheckbox.isChecked = true
        showDividerCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                eventLine.visibility = View.VISIBLE
            } else {
                eventLine.visibility = View.GONE
            }
        }
        daysCheckbox.isChecked = true

        yearsCheckbox.setOnCheckedChangeListener(checkBoxListener)
        monthsCheckbox.setOnCheckedChangeListener(checkBoxListener)
        weeksCheckbox.setOnCheckedChangeListener(checkBoxListener)
        daysCheckbox.setOnCheckedChangeListener(checkBoxListener)
    }

    private val checkBoxListener = CompoundButton.OnCheckedChangeListener { _, _ ->
        if (!yearsCheckbox.isChecked && !monthsCheckbox.isChecked && !weeksCheckbox.isChecked && !daysCheckbox.isChecked) {
            toast(getString(R.string.add_activity_toast_checkbox))
            daysCheckbox.isChecked = true
        }
        eventCalculateText.text = generateCounterText()
    }

    private fun setUpEditTexts() {
        titleEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    eventTitle.text = it.toString()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //not used
            }
        })
    }

    private fun setUpSeekBar() {
        pictureDimSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                eventImage.setColorFilter(Color.argb(255 / 17 * progress, 0, 0, 0))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //nothing
            }
        })
    }

    private fun setUpFontPicker() {
        colorImageView.setOnClickListener {
            displayColorPicker()
        }
    }

    private fun displayColorPicker() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(10)
                .setPositiveButton("ok", { _, selectedColor, _ -> changeWidgetsColors(selectedColor) })
                .setNegativeButton("cancel", { _, _ -> })
                .build()
                .show()
    }

    private fun changeWidgetsColors(color: Int) {
        eventTitle.textColor = color
        eventCalculateText.textColor = color
        colorImageView.backgroundColor = color
        eventLine.backgroundColor = color
    }

    private fun setUpOnClickListeners() {
        dateEditText.setOnClickListener(showDatePicker)
        reminderDateEditText.setOnClickListener(showReminderDatePicker)
        clearReminderDateButton.setOnClickListener {
            reminderDateEditText.setText("")
            reminderDate = ""
            chosenReminderHour = 0
            chosenReminderMinute = 0
        }
    }

    private val showDatePicker = View.OnClickListener {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, chosenYear, chosenMonth, chosenDay ->
            this.chosenYear = chosenYear
            this.chosenMonth = chosenMonth
            this.chosenDay = chosenDay
            dateEditText.setText(formatDate(chosenYear, chosenMonth, chosenDay))
            eventCalculateText.text = generateCounterText()
        }, year, month, day).show()
    }

    private val showReminderDatePicker = View.OnClickListener {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, chosenYear, chosenMonth, chosenDay ->
            reminderDate = formatDate(chosenYear, chosenMonth, chosenDay)
            displayTimePickerDialog()
        }, year, month, day).show()
    }

    private fun displayTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, chosenHour, chosenMinute ->
            this.chosenReminderHour = chosenHour
            this.chosenReminderMinute = chosenMinute
            val time = formatTime(chosenHour, chosenMinute)
            reminderDate += " $time"
            reminderDateEditText.setText(reminderDate)
        }, hour, minute, true).show()
    }

    private fun generateCounterText(): String {
        return calculateDate(chosenYear, chosenMonth + 1, chosenDay,
                yearsCheckbox.isChecked,
                monthsCheckbox.isChecked,
                weeksCheckbox.isChecked,
                daysCheckbox.isChecked,
                this)
    }

}