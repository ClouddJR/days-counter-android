package com.arkadiusz.dayscounter.ui.addeditevent

import PreferenceUtils.defaultPrefs
import PreferenceUtils.get
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.arkadiusz.dayscounter.Provider.AppWidgetProvider
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.data.local.DatabaseProvider
import com.arkadiusz.dayscounter.ui.internetgallery.InternetGalleryActivity
import com.arkadiusz.dayscounter.ui.localgallery.GalleryActivity
import com.arkadiusz.dayscounter.utils.*
import com.arkadiusz.dayscounter.utils.DateUtils.formatDate
import com.arkadiusz.dayscounter.utils.DateUtils.formatDateAccordingToSettings
import com.arkadiusz.dayscounter.utils.DateUtils.formatTime
import com.arkadiusz.dayscounter.utils.DateUtils.getElementsFromDate
import com.arkadiusz.dayscounter.utils.PurchasesUtils.displayPremiumInfoDialog
import com.arkadiusz.dayscounter.utils.PurchasesUtils.isPremiumUser
import com.bumptech.glide.Glide
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.content_add.*
import org.jetbrains.anko.*
import java.io.File
import java.util.*

/**
 * Created by arkadiusz on 31.03.18
 */

class EditActivity : AppCompatActivity() {

    private var unformattedDate = ""

    private lateinit var passedEvent: Event

    private lateinit var eventType: String
    private var hasAlarm = false
    private var selectedColor = -1
    private var dimValue = 4

    private val pickPhotoGallery = 1
    private val pickPhotoInternet = 2
    private val writeRequestCode = 1234

    private var imageUri: Uri? = null
    private var imageID = 0
    private var imageColor = 0

    private var chosenYear = 0
    private var chosenMonth = 0
    private var chosenDay = 0

    private var reminderDate = ""
    private var chosenReminderYear = 0
    private var chosenReminderMonth = 0
    private var chosenReminderDay = 0
    private var chosenReminderHour = 0
    private var chosenReminderMinute = 0

    private var wasTimePickerAlreadyDisplayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(false, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_add)
        receivePassedEventId()
        setUpSpinners()
        setUpCheckboxes()
        setUpEditTexts()
        setUpSeekBar()
        setUpFontPicker()
        setUpOnClickListeners()
        setUpImageChoosing()
        fillFormWithPassedData()
        changeAddButtonName()
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


    private fun receivePassedEventId() {
        val passedEventId = intent.getStringExtra("eventId")
        passedEvent = DatabaseProvider.provideRepository().getEventById(passedEventId)!!
    }

    private fun setUpSpinners() {
        val fontSizeAdapter = ArrayAdapter.createFromResource(this, R.array.add_activity_font_size, R.layout.support_simple_spinner_dropdown_item)
        counterFontSizeSpinner.adapter = fontSizeAdapter
        titleFontSizeSpinner.adapter = fontSizeAdapter

        val repetitionAdapter = ArrayAdapter.createFromResource(this, R.array.add_activity_repeat, R.layout.support_simple_spinner_dropdown_item)
        repeatSpinner.adapter = repetitionAdapter

        val fontTypeAdapter = FontTypeSpinnerAdapter(this, R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.font_type).toList())
        fontTypeSpinner.adapter = fontTypeAdapter

        setSpinnersListeners()
    }

    private fun setSpinnersListeners() {
        counterFontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if ((view as? TextView)?.text != null) {
                    eventCalculateText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (view as? TextView)?.text.toString().toFloat())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing
            }
        }

        titleFontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if ((view as? TextView)?.text != null) {
                    eventTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (view as? TextView)?.text.toString().toFloat())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing
            }
        }
        fontTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if ((view as? TextView)?.text != null) {
                    val fontName = (view as? TextView)?.text.toString()
                    val typeFace = FontUtils.getFontFor(fontName, this@EditActivity)
                    eventTitle.typeface = typeFace
                    eventCalculateText.typeface = typeFace
                }
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
        //daysCheckbox.isChecked = true

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
                dimValue = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //not used
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //not used
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

    private fun displayColorPickerForEventBackground() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(10)
                .setPositiveButton("ok", { _, selectedColor, _ -> changeEventColor(selectedColor) })
                .setNegativeButton("cancel", { _, _ -> })
                .build()
                .show()
    }

    private fun changeWidgetsColors(color: Int) {
        selectedColor = color
        eventTitle.textColor = color
        eventCalculateText.textColor = color
        colorImageView.backgroundColor = color
        eventLine.backgroundColor = color
    }

    private fun changeEventColor(color: Int) {
        imageID = 0
        imageUri = null
        imageColor = color
        eventImage.setImageDrawable(null)
        eventImage.backgroundColor = color
    }

    private fun setUpOnClickListeners() {
        dateEditText.setOnClickListener(showDatePicker)
        reminderDateEditText.setOnClickListener(showReminderDatePicker)
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
            val eventToBeAdded = prepareEventBasedOnViews()
            DatabaseProvider.provideRepository().editEvent(eventToBeAdded)
            addReminder(eventToBeAdded)
            updateWidgetIfOnScreen(eventToBeAdded.widgetID)
            updateWidgetIfOnScreen(eventToBeAdded.widgetID)
            finish()
        }
    }

    private fun updateWidgetIfOnScreen(id: Int) {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, applicationContext, AppWidgetProvider::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(id))
        sendBroadcast(intent)
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
            unformattedDate = formatDate(chosenYear, chosenMonth, chosenDay)
            dateEditText.setText(formatDateAccordingToSettings(unformattedDate,
                    defaultPrefs(this)["dateFormat"] ?: ""))
            eventCalculateText.text = generateCounterText()
        }, year, month, day).show()
    }

    private val showReminderDatePicker = View.OnClickListener {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        wasTimePickerAlreadyDisplayed = false
        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, chosenYear, chosenMonth, chosenDay ->
            reminderDate = DateUtils.formatDate(chosenYear, chosenMonth, chosenDay)
            chosenReminderYear = chosenYear
            chosenReminderMonth = chosenMonth
            chosenReminderDay = chosenDay
            if (!wasTimePickerAlreadyDisplayed) {
                displayTimePickerDialog()
            }
        }, year, month, day).show()
    }

    private fun displayTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        wasTimePickerAlreadyDisplayed = true
        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, chosenHour, chosenMinute ->
            if (view.isShown) {
                this.chosenReminderHour = chosenHour
                this.chosenReminderMinute = chosenMinute
                val time = DateUtils.formatTime(chosenHour, chosenMinute)
                reminderDate = formatDateAccordingToSettings(reminderDate,
                        defaultPrefs(this)["dateFormat"] ?: "")
                reminderDate += " $time"
                reminderDateEditText.setText(reminderDate)
                hasAlarm = true
            }

        }, hour, minute, true).show()
    }

    private fun generateCounterText(): String {
        return DateUtils.calculateDate(chosenYear, chosenMonth + 1, chosenDay,
                yearsCheckbox.isChecked,
                monthsCheckbox.isChecked,
                weeksCheckbox.isChecked,
                daysCheckbox.isChecked,
                this)
    }


    private fun prepareEventBasedOnViews(): Event {
        val event = Event()
        event.id = passedEvent.id
        event.name = titleEditText.text.toString()
        event.date = unformattedDate
        event.description = descriptionEditText.text.toString()
        event.image = imageUri.toString()
        event.imageID = imageID
        event.imageColor = imageColor
        event.imageCloudPath = passedEvent.imageCloudPath
        event.type = eventType
        event.repeat = repeatSpinner.selectedItemPosition.toString()
        when (hasAlarm) {
            true -> {
                event.hasAlarm = true
                event.reminderYear = chosenReminderYear
                event.reminderMonth = chosenReminderMonth
                event.reminderDay = chosenReminderDay
                event.reminderHour = chosenReminderHour
                event.reminderMinute = chosenReminderMinute
                event.notificationText = reminderTextEditText.text.toString()
            }
            else -> event.hasAlarm = false
        }
        event.widgetID = passedEvent.widgetID
        event.hasTransparentWidget = passedEvent.hasTransparentWidget
        event.formatYearsSelected = yearsCheckbox.isChecked
        event.formatMonthsSelected = monthsCheckbox.isChecked
        event.formatWeeksSelected = weeksCheckbox.isChecked
        event.formatDaysSelected = daysCheckbox.isChecked
        event.lineDividerSelected = showDividerCheckbox.isChecked
        event.counterFontSize = (counterFontSizeSpinner.getChildAt(0) as TextView).text.toString().toInt()
        event.titleFontSize = (titleFontSizeSpinner.getChildAt(0) as TextView).text.toString().toInt()
        event.fontType = (fontTypeSpinner.getChildAt(0) as TextView).text.toString()
        event.fontColor = selectedColor
        event.pictureDim = dimValue
        return event
    }

    private fun addReminder(eventToBeAdded: Event) {
        if (isReminderSet()) {
            RemindersUtils.addNewReminder(this, eventToBeAdded)
        }
    }

    private fun isReminderSet(): Boolean = chosenReminderYear != 0 && chosenReminderDay != 0

    private fun setUpImageChoosing() {
        imageChooserButton.setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_activity_dialog_title))
                    .setItems(setUpImageChooserDialogOptions()) { _, which ->
                        when (which) {
                            0 -> askForPermissionsAndDisplayCropActivity()
                            1 -> startActivityForResult<GalleryActivity>(pickPhotoGallery, "activity" to "Edit")
                            2 -> displayColorPickerForEventBackground()
                            3 -> {
                                if (isPremiumUser(this)) {
                                    askForPermissionsAndDisplayInternetImageActivity()
                                } else {
                                    displayPremiumInfoDialog(this)
                                }
                            }
                        }
                    }.show()
        }
    }

    private fun setUpImageChooserDialogOptions(): Array<String> {
        val options = mutableListOf<String>(getString(R.string.add_activity_dialog_option_custom),
                getString(R.string.add_activity_dialog_option_gallery),
                getString(R.string.add_activity_dialog_option_color),
                getString(R.string.add_activity_dialog_option_internet))
        return options.toTypedArray()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        //intent from GalleryActivity
        imageID = intent?.getIntExtra("imageID", 0) ?: 0
        if (imageID != 0) {
            imageColor = 0
            imageUri = null
            Glide.with(this).load(imageID).into(eventImage)
        }

        //intent from InternetGalleryActivity
        val internetImageUri = intent?.getStringExtra("internetImageUri") ?: ""
        if (internetImageUri.isNotEmpty()) {
            imageUri = Uri.parse(internetImageUri)
            imageColor = 0
            imageID = 0
            Glide.with(this).load(File(imageUri?.path)).into(eventImage)
        }

    }

    private fun askForPermissionsAndDisplayCropActivity() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), writeRequestCode)
        } else {
            //permission already granted, so display crop dialog
            CropImage.startPickImageActivity(this)
        }
    }

    private fun askForPermissionsAndDisplayInternetImageActivity() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), pickPhotoInternet)
        } else {
            startActivityForResult<InternetGalleryActivity>(pickPhotoInternet, "activity" to "Edit")
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == writeRequestCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            CropImage.startPickImageActivity(this)
        }

        if (requestCode == pickPhotoInternet && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult<InternetGalleryActivity>(pickPhotoInternet, "activity" to "Edit")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageData: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageData)

        if (isPossibleToOpenCropActivityAfterChoosingImage(requestCode, resultCode)) {
            val chosenImageUri = CropImage.getPickImageResultUri(this, imageData)
            if (Build.VERSION.SDK_INT >= 23) {
                checkForReadingExternalStoragePermissionsAndStartCropActivity(chosenImageUri)
            } else {
                startCropImageActivity(chosenImageUri)
            }
        }

        if (isResultComingWithImageAfterCropping(requestCode)) {
            displayImageHere(imageData)
        }
    }

    private fun isPossibleToOpenCropActivityAfterChoosingImage(requestCode: Int, resultCode: Int): Boolean {
        return requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkForReadingExternalStoragePermissionsAndStartCropActivity(chosenImageUri: Uri) {
        if (CropImage.isReadExternalStoragePermissionsRequired(this, chosenImageUri)) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE)
        } else {
            startCropImageActivity(chosenImageUri)
        }
    }

    private fun startCropImageActivity(imageUri: Uri) {
        CropImage.activity(imageUri)
                .setAspectRatio(18, 9)
                .setFixAspectRatio(true)
                .start(this)
    }

    private fun isResultComingWithImageAfterCropping(requestCode: Int): Boolean {
        return requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
    }

    private fun displayImageHere(data: Intent?) {
        data?.let {
            imageColor = 0
            imageID = 0
            imageUri = CropImage.getActivityResult(data).uri as Uri
            imageUri = StorageUtils.saveFile(imageUri as Uri)
            Glide.with(this).load(File(imageUri?.path)).into(eventImage)
        }
    }

    private fun fillFormWithPassedData() {
        fillGeneralSectionForm()
        fillReminderSectionForm()
        fillRepetitionSectionForm()
        fillCounterSectionForm()
        fillFontSectionForm()
        fillPictureDimSectionForm()
        displayEventImage()
        assignAdditionalParameters()
    }

    private fun fillGeneralSectionForm() {
        titleEditText.setText(passedEvent.name)
        dateEditText.setText(formatDateAccordingToSettings(passedEvent.date,
                defaultPrefs(this)["dateFormat"] ?: ""))
        descriptionEditText.setText(passedEvent.description)
        val dateTriple = getElementsFromDate(passedEvent.date)
        chosenYear = dateTriple.first
        chosenMonth = dateTriple.second - 1
        chosenDay = dateTriple.third
        unformattedDate = formatDate(chosenYear, chosenMonth, chosenDay)
    }

    private fun fillReminderSectionForm() {
        if (passedEvent.reminderYear != 0) {
            chosenReminderYear = passedEvent.reminderYear
            chosenReminderMonth = passedEvent.reminderMonth
            chosenReminderDay = passedEvent.reminderDay
            chosenReminderHour = passedEvent.reminderHour
            chosenReminderMinute = passedEvent.reminderMinute
            hasAlarm = true
            val reminderDate = formatDateAccordingToSettings(formatDate(passedEvent.reminderYear,
                    passedEvent.reminderMonth,
                    passedEvent.reminderDay), defaultPrefs(this)["dateFormat"] ?: "") + ", " +
                    formatTime(passedEvent.reminderHour, passedEvent.reminderMinute)

            reminderDateEditText.setText(reminderDate)
            reminderTextEditText.setText(passedEvent.notificationText)
        }
    }

    private fun fillRepetitionSectionForm() {
        repeatSpinner.setSelection(passedEvent.repeat.toInt())
    }

    private fun fillCounterSectionForm() {
        yearsCheckbox.isChecked = passedEvent.formatYearsSelected
        monthsCheckbox.isChecked = passedEvent.formatMonthsSelected
        weeksCheckbox.isChecked = passedEvent.formatWeeksSelected
        daysCheckbox.isChecked = passedEvent.formatDaysSelected
    }

    private fun fillFontSectionForm() {
        showDividerCheckbox.isChecked = passedEvent.lineDividerSelected
        counterFontSizeSpinner.setSelection(getSpinnerIndexFor(passedEvent.counterFontSize, counterFontSizeSpinner))
        titleFontSizeSpinner.setSelection(getSpinnerIndexFor(passedEvent.titleFontSize, titleFontSizeSpinner))
        fontTypeSpinner.setSelection(FontUtils.getFontPositionFor(passedEvent.fontType))
        colorImageView.backgroundColor = passedEvent.fontColor
        changeWidgetsColors(passedEvent.fontColor)
    }

    private fun fillPictureDimSectionForm() {
        pictureDimSeekBar.progress = passedEvent.pictureDim
    }

    private fun displayEventImage() {
        when {
            passedEvent.imageColor != 0 -> changeEventColor(passedEvent.imageColor)
            passedEvent.imageID != 0 -> {
                imageID = passedEvent.imageID
                Glide.with(this).load(imageID).into(eventImage)
            }
            else -> {
                imageUri = Uri.parse(passedEvent.image)
                when {
                    File(passedEvent.image).exists() -> Glide.with(this).load(passedEvent.image).into(eventImage)
                    passedEvent.imageCloudPath.isNotEmpty() -> Glide.with(this).load(
                            FirebaseStorage.getInstance().getReference(passedEvent.imageCloudPath))
                            .into(eventImage)
                    else -> Glide.with(this).load(android.R.color.darker_gray)
                            .into(eventImage)
                }
            }
        }
    }

    private fun assignAdditionalParameters() {
        eventType = passedEvent.type
    }


    private fun getSpinnerIndexFor(fontSize: Int, spinner: Spinner): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == fontSize.toString()) {
                return i
            }
        }
        return 6 //default font size
    }

    private fun changeAddButtonName() {
        addButton.text = getString(R.string.add_activity_button_title)
    }
}