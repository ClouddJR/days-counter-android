package com.arkadiusz.dayscounter.activities

import PreferenceUtils.defaultPrefs
import PreferenceUtils.get
import PreferenceUtils.set
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.adapters.FontTypeSpinnerAdapter
import com.arkadiusz.dayscounter.model.Event
import com.arkadiusz.dayscounter.repositories.DatabaseProvider
import com.arkadiusz.dayscounter.repositories.FirebaseRepository
import com.arkadiusz.dayscounter.utils.DateUtils.calculateDate
import com.arkadiusz.dayscounter.utils.DateUtils.formatDate
import com.arkadiusz.dayscounter.utils.DateUtils.formatDateAccordingToSettings
import com.arkadiusz.dayscounter.utils.DateUtils.formatTime
import com.arkadiusz.dayscounter.utils.DateUtils.generateTodayCalendar
import com.arkadiusz.dayscounter.utils.FontUtils
import com.arkadiusz.dayscounter.utils.PurchasesUtils.displayPremiumInfoDialog
import com.arkadiusz.dayscounter.utils.PurchasesUtils.isPremiumUser
import com.arkadiusz.dayscounter.utils.RemindersUtils
import com.arkadiusz.dayscounter.utils.StorageUtils.saveFile
import com.arkadiusz.dayscounter.utils.ThemeUtils
import com.bumptech.glide.Glide
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.content_add.*
import org.jetbrains.anko.*
import java.io.File
import java.util.*


/**
 * Created by arkadiusz on 04.03.18
 */

class AddActivity : AppCompatActivity() {

    private var unformattedDate = ""

    private lateinit var eventType: String
    private var hasAlarm = false
    private var selectedColor = -1
    private var dimValue = 4

    private val pickPhotoGallery = 1
    private val pickPhotoInternet = 2
    private val writeRequestCode = 1234

    private var imageUri: Uri? = null
    private var imageID = 2131230778
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

    private lateinit var interstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(false, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_add)
        receiveEventType()
        setCurrentDateInForm()
        setUpSpinners()
        setUpCheckboxes()
        setUpEditTexts()
        setUpSeekBar()
        setUpFontPicker()
        setUpOnClickListeners()
        setUpImageChoosing()
        setUpAd()
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

    private fun receiveEventType() {
        eventType = intent.getStringExtra("Event Type")
    }

    private fun setCurrentDateInForm() {
        val calendar = generateTodayCalendar()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        chosenYear = year
        chosenMonth = month
        chosenDay = day
        unformattedDate = formatDate(year, month, day)
        dateEditText.setText(formatDateAccordingToSettings(unformattedDate,
                defaultPrefs(this)["dateFormat"] ?: ""))
        eventCalculateText.text = generateCounterText()
    }

    private fun setUpSpinners() {
        val fontSizeAdapter = ArrayAdapter.createFromResource(this, R.array.add_activity_font_size, R.layout.support_simple_spinner_dropdown_item)
        counterFontSizeSpinner.adapter = fontSizeAdapter
        titleFontSizeSpinner.adapter = fontSizeAdapter
        counterFontSizeSpinner.setSelection(6)
        titleFontSizeSpinner.setSelection(5)

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
                eventCalculateText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (view as? TextView)?.text.toString().toFloat())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing
            }
        }

        titleFontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                eventTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (view as? TextView)?.text.toString().toFloat())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing
            }
        }
        fontTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val fontName = (view as? TextView)?.text.toString()
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
            showAd()
            val eventToBeAdded = prepareEventBasedOnViews()
            val id = DatabaseProvider.provideRepository().addEventToDatabase(eventToBeAdded)
            FirebaseRepository().addOrEditEventInFirebase(defaultPrefs(this)["firebase-email"]
                    ?: "", eventToBeAdded, id)
            addReminder(eventToBeAdded)
            finish()
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
            reminderDate = formatDate(chosenYear, chosenMonth, chosenDay)
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
                val time = formatTime(chosenHour, chosenMinute)
                reminderDate = formatDateAccordingToSettings(reminderDate,
                        defaultPrefs(this)["dateFormat"] ?: "")
                reminderDate += " $time"
                reminderDateEditText.setText(reminderDate)
                hasAlarm = true
            }
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


    private fun prepareEventBasedOnViews(): Event {
        val event = Event()
        event.name = titleEditText.text.toString()
        event.date = unformattedDate
        event.description = descriptionEditText.text.toString()
        event.image = imageUri.toString()
        event.imageID = imageID
        event.imageColor = imageColor
        event.type = eventType
        event.repeat = repeatSpinner.selectedItemPosition.toString()
        if (hasAlarm) {
            event.hasAlarm = true
            event.reminderYear = chosenReminderYear
            event.reminderMonth = chosenReminderMonth
            event.reminderDay = chosenReminderDay
            event.reminderHour = chosenReminderHour
            event.reminderMinute = chosenReminderMinute
            event.notificationText = reminderTextEditText.text.toString()
        } else {
            event.hasAlarm = false
        }

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
                            1 -> startActivityForResult<GalleryActivity>(pickPhotoGallery, "activity" to "Add")
                            2 -> displayColorPickerForEventBackground()
                            3 -> {
                                if (!isPremiumUser(this)) {
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
            Picasso.with(this).load(imageID).resize(0, 700).into(eventImage)
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
            startActivityForResult<InternetGalleryActivity>(pickPhotoInternet, "activity" to "Add")
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == writeRequestCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            CropImage.startPickImageActivity(this)
        }

        if (requestCode == pickPhotoInternet && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult<InternetGalleryActivity>(pickPhotoInternet, "activity" to "Add")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageData: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageData)

        if (isComingFromInternetActivity(requestCode, resultCode)) {
            startCropImageActivity(imageData!!.data!!)
        }

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

    private fun isComingFromInternetActivity(requestCode: Int, resultCode: Int): Boolean {
        return requestCode == pickPhotoInternet && resultCode == Activity.RESULT_OK
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
            imageUri = saveFile(imageUri as Uri)
            Glide.with(this).load(File(imageUri?.path)).into(eventImage)
        }
    }

    private fun setUpAd() {
        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = "ca-app-pub-4098342918729972/3144606816"

        val prefs = defaultPrefs(this)
        val areAdsRemoved: Boolean = prefs["ads", false] ?: false
        val wasShown: Boolean = prefs["wasAdShown", true] ?: true

        if (!areAdsRemoved && !wasShown) {
            interstitialAd.loadAd(AdRequest.Builder().build())
        }
    }

    private fun showAd() {
        val prefs = defaultPrefs(this)
        if (interstitialAd.isLoaded) {
            interstitialAd.show()
            prefs["wasAdShown"] = true
        } else {
            prefs["wasAdShown"] = false
        }
    }

}