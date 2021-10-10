package com.arkadiusz.dayscounter.ui.addeditevent

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.ui.internetgallery.InternetGalleryActivity
import com.arkadiusz.dayscounter.ui.localgallery.GalleryActivity
import com.arkadiusz.dayscounter.util.*
import com.arkadiusz.dayscounter.util.DateUtils.calculateDate
import com.arkadiusz.dayscounter.util.DateUtils.formatDate
import com.arkadiusz.dayscounter.util.DateUtils.formatDateAccordingToSettings
import com.arkadiusz.dayscounter.util.DateUtils.formatTime
import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import com.bumptech.glide.Glide
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.content_add.*
import org.jetbrains.anko.*
import java.io.File
import java.util.*

abstract class BaseAddActivity : AppCompatActivity() {

    protected val viewModel: AddEditViewModel by viewModels()

    private val requestPermissionForInternetActivity =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startActivityForResult<InternetGalleryActivity>(
                    pickPhotoInternet,
                    "activity" to "Add"
                )
            }
        }

    private val requestPermissionForPickingImage =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                CropImage.startPickImageActivity(this)
            }
        }

    protected var date = ""

    protected var hasAlarm = false
    protected var selectedColor = -1
    protected var dimValue = 4

    protected val pickPhotoGallery = 1
    protected val pickPhotoInternet = 2

    protected var imageUri: Uri? = null
    protected var imageID = 2131230778
    protected var imageColor = 0

    protected var chosenYear = 0
    protected var chosenMonth = 0
    protected var chosenDay = 0

    protected var reminderDate = ""
    protected var chosenReminderYear = 0
    protected var chosenReminderMonth = 0
    protected var chosenReminderDay = 0
    protected var chosenReminderHour = 0
    protected var chosenReminderMinute = 0

    protected var wasTimePickerAlreadyDisplayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(false, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_add)
        setUpSpinners()
        setUpCheckboxes()
        setUpEditTexts()
        setUpSeekBar()
        setUpFontPicker()
        setUpOnClickListeners()
        setUpImageChoosing()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // Intent from GalleryActivity
        imageID = intent?.getIntExtra("imageID", 0) ?: 0
        if (imageID != 0) {
            imageColor = 0
            imageUri = null
            Glide.with(this).load(imageID).into(eventImage)
        }

        // Intent from InternetGalleryActivity
        val internetImageUri = intent?.getStringExtra("internetImageUri") ?: ""
        if (internetImageUri.isNotEmpty()) {
            imageUri = Uri.parse(internetImageUri)
            imageColor = 0
            imageID = 0
            Glide.with(this).load(File(imageUri?.path)).into(eventImage)
        }
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

    abstract fun handleSaveClick()

    private fun setUpSpinners() {
        val fontSizeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.add_activity_font_size,
            R.layout.support_simple_spinner_dropdown_item
        )
        counterFontSizeSpinner.adapter = fontSizeAdapter
        titleFontSizeSpinner.adapter = fontSizeAdapter
        counterFontSizeSpinner.setSelection(6)
        titleFontSizeSpinner.setSelection(5)

        repeatSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.add_activity_repeat,
            R.layout.support_simple_spinner_dropdown_item
        )
        repeatSpinner.setSelection(0)

        fontTypeSpinner.adapter = FontTypeSpinnerAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.font_type).toList()
        )
        fontTypeSpinner.setSelection(8)

        setSpinnersListeners()
    }

    private fun setSpinnersListeners() {
        counterFontSizeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    (view as? TextView)?.text?.let { text ->
                        eventCalculateText.setTextSize(
                            TypedValue.COMPLEX_UNIT_DIP,
                            text.toString().toFloat()
                        )
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // nop
                }
            }

        titleFontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (view as? TextView)?.text?.let { text ->
                    eventTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text.toString().toFloat())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nop
            }
        }

        fontTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (view as? TextView)?.text?.let { text ->
                    val fontName = text.toString()
                    val typeFace = FontUtils.getFontFor(fontName, this@BaseAddActivity)
                    eventTitle.typeface = typeFace
                    eventCalculateText.typeface = typeFace
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nop
            }
        }
    }

    private fun setUpCheckboxes() {
        showDividerCheckbox.isChecked = true
        showDividerCheckbox.setOnCheckedChangeListener { _, isChecked ->
            eventLine.isVisible = isChecked
        }
        daysCheckbox.isChecked = true

        yearsCheckbox.setOnCheckedChangeListener(checkBoxListener)
        monthsCheckbox.setOnCheckedChangeListener(checkBoxListener)
        weeksCheckbox.setOnCheckedChangeListener(checkBoxListener)
        daysCheckbox.setOnCheckedChangeListener(checkBoxListener)
    }

    private val checkBoxListener = CompoundButton.OnCheckedChangeListener { _, _ ->
        if (noCheckboxIsSelected()) {
            toast(getString(R.string.add_activity_toast_checkbox))
            daysCheckbox.isChecked = true
        }
        eventCalculateText.text = generateCounterText()
    }

    private fun noCheckboxIsSelected(): Boolean {
        return !yearsCheckbox.isChecked
                && !monthsCheckbox.isChecked
                && !weeksCheckbox.isChecked
                && !daysCheckbox.isChecked
    }

    private fun setUpEditTexts() {
        titleEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(e: Editable?) {
                e?.let {
                    eventTitle.text = it.toString()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nop
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // nop
            }
        })
    }

    private fun setUpSeekBar() {
        pictureDimSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                eventImage.setColorFilter(Color.argb(255 / 17 * progress, 0, 0, 0))
                dimValue = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // nop
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // nop
            }
        })
    }

    private fun setUpFontPicker() {
        colorImageView.setOnClickListener {
            displayColorPicker()
        }
    }

    private fun displayColorPicker() {
        val picker = ColorPickerDialogBuilder
            .with(this)
            .setTitle("Choose color")
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
            .density(10)
            .setPositiveButton("ok") { _, selectedColor, _ -> changeWidgetsColors(selectedColor) }
            .setNegativeButton("cancel") { _, _ -> }

        if (selectedColor != -1) picker.initialColor(selectedColor)

        picker.build()
            .show()
    }

    private fun changeWidgetsColors(color: Int) {
        selectedColor = color
        eventTitle.textColor = color
        eventCalculateText.textColor = color
        colorImageView.backgroundColor = color
        eventLine.backgroundColor = color
    }

    private fun setUpOnClickListeners() {
        dateEditText.setOnClickListener { showDatePicker() }
        reminderDateEditText.setOnClickListener { showReminderDatePicker() }
        clearReminderDateButton.setOnClickListener {
            reminderDateEditText.setText("")
            chosenReminderYear = 0
            chosenReminderMonth = 0
            chosenReminderDay = 0
            chosenReminderHour = 0
            chosenReminderMinute = 0
            hasAlarm = false
        }
        addButton.setOnClickListener {
            handleSaveClick()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this, { _, chosenYear, chosenMonth, chosenDay ->
                this.chosenYear = chosenYear
                this.chosenMonth = chosenMonth
                this.chosenDay = chosenDay

                date = formatDate(chosenYear, chosenMonth, chosenDay)

                dateEditText.setText(
                    formatDateAccordingToSettings(
                        date,
                        defaultPrefs(this)["dateFormat"] ?: ""
                    )
                )

                eventCalculateText.text = generateCounterText()
            },
            if (chosenYear == 0) calendar.get(Calendar.YEAR) else chosenYear,
            if (chosenYear == 0) calendar.get(Calendar.MONTH) else chosenMonth,
            if (chosenYear == 0) calendar.get(Calendar.DAY_OF_MONTH) else chosenDay
        ).show()
    }

    private fun showReminderDatePicker() {
        val calendar = Calendar.getInstance()

        wasTimePickerAlreadyDisplayed = false
        DatePickerDialog(
            this, { _, chosenYear, chosenMonth, chosenDay ->
                reminderDate = formatDate(chosenYear, chosenMonth, chosenDay)
                chosenReminderYear = chosenYear
                chosenReminderMonth = chosenMonth
                chosenReminderDay = chosenDay

                if (!wasTimePickerAlreadyDisplayed) {
                    displayTimePickerDialog()
                }
            },
            if (chosenReminderYear == 0) calendar.get(Calendar.YEAR) else chosenReminderYear,
            if (chosenReminderYear == 0) calendar.get(Calendar.MONTH) else chosenReminderMonth,
            if (chosenReminderYear == 0) calendar.get(Calendar.DAY_OF_MONTH) else chosenReminderDay
        ).show()
    }

    private fun displayTimePickerDialog() {
        val calendar = Calendar.getInstance()

        wasTimePickerAlreadyDisplayed = true
        TimePickerDialog(
            this, { view, chosenHour, chosenMinute ->
                if (view.isShown) {
                    this.chosenReminderHour = chosenHour
                    this.chosenReminderMinute = chosenMinute
                    reminderDate = formatDateAccordingToSettings(
                        reminderDate,
                        defaultPrefs(this)["dateFormat"] ?: ""
                    )
                    reminderDate += " ${formatTime(chosenHour, chosenMinute)}"
                    reminderDateEditText.setText(reminderDate)
                    hasAlarm = true
                }
            },
            if (chosenReminderHour == 0) calendar.get(Calendar.HOUR_OF_DAY) else chosenReminderHour,
            if (chosenReminderHour == 0) calendar.get(Calendar.MINUTE) else chosenReminderMinute,
            true
        ).show()
    }

    private fun setUpImageChoosing() {
        imageChooserButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_activity_dialog_title))
                .setItems(setUpImageChooserDialogOptions()) { _, which ->
                    when (which) {
                        0 -> askForPermissionAndDisplayCropActivity()
                        1 -> startActivityForResult<GalleryActivity>(
                            pickPhotoGallery,
                            "activity" to "Add"
                        )
                        2 -> displayColorPickerForEventBackground()
                        3 -> {
                            if (PurchasesUtils.isPremiumUser(this)) {
                                askForPermissionAndDisplayInternetImageActivity()
                            } else {
                                PurchasesUtils.displayPremiumInfoDialog(this)
                            }
                        }
                    }
                }.show()
        }
    }

    private fun setUpImageChooserDialogOptions(): Array<String> {
        val options = listOf(
            getString(R.string.add_activity_dialog_option_custom),
            getString(R.string.add_activity_dialog_option_gallery),
            getString(R.string.add_activity_dialog_option_color),
            getString(R.string.add_activity_dialog_option_internet)
        )
        return options.toTypedArray()
    }

    private fun askForPermissionAndDisplayCropActivity() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionForPickingImage.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            CropImage.startPickImageActivity(this)
        }
    }

    private fun displayColorPickerForEventBackground() {
        val picker = ColorPickerDialogBuilder
            .with(this)
            .setTitle("Choose color")
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
            .density(10)
            .setPositiveButton("ok") { _, selectedColor, _ -> changeEventColor(selectedColor) }
            .setNegativeButton("cancel") { _, _ -> }

        if (imageColor != -0) picker.initialColor(imageColor)

        picker.build()
            .show()
    }

    private fun changeEventColor(color: Int) {
        imageID = 0
        imageUri = null
        imageColor = color
        eventImage.setImageDrawable(null)
        eventImage.backgroundColor = color
    }

    private fun askForPermissionAndDisplayInternetImageActivity() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionForInternetActivity.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            startActivityForResult<InternetGalleryActivity>(pickPhotoInternet, "activity" to "Add")
        }
    }

    protected fun displayImageHere(data: Intent?) {
        data?.let {
            imageColor = 0
            imageID = 0
            imageUri = CropImage.getActivityResult(data).uri as Uri
            imageUri = StorageUtils.saveImage(this, imageUri as Uri)
            Glide.with(this).load(File(imageUri?.path)).into(eventImage)
        }
    }

    protected fun generateCounterText(): String {
        return calculateDate(
            chosenYear, chosenMonth + 1, chosenDay,
            yearsCheckbox.isChecked,
            monthsCheckbox.isChecked,
            weeksCheckbox.isChecked,
            daysCheckbox.isChecked,
            this
        )
    }

    protected fun addReminder(eventToBeAdded: Event) {
        if (isReminderSet()) {
            RemindersUtils.addNewReminder(this, eventToBeAdded)
        }
    }

    private fun isReminderSet(): Boolean = chosenReminderYear != 0 && chosenReminderDay != 0
}