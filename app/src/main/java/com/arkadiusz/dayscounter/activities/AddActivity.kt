package com.arkadiusz.dayscounter.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.utils.DateUtils.formatDate
import com.arkadiusz.dayscounter.utils.DateUtils.formatTime
import org.jetbrains.anko.alert
import java.util.*

/**
 * Created by arkadiusz on 04.03.18
 */

class AddActivity_R : AppCompatActivity() {

    private var reminderDate = ""
    private var chosenHour = 0
    private var chosenMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_add_r)
        setUpSpinners()

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
        val fontSizeAdapter = ArrayAdapter.createFromResource(this,R.array.add_activity_font_size,)
    }

    private val showDatePicker = View.OnClickListener {
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
            this.chosenHour = chosenHour
            this.chosenMinute = chosenMinute
            val time = formatTime(chosenHour, chosenMinute)
            reminderDate += " $time"
        }, hour, minute, true).show()
    }

}