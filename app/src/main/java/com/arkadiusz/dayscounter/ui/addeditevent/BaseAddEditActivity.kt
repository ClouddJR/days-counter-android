package com.arkadiusz.dayscounter.ui.addeditevent

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.Event
import com.arkadiusz.dayscounter.ui.crop.CropActivity
import com.arkadiusz.dayscounter.ui.internetgallery.InternetGalleryActivity
import com.arkadiusz.dayscounter.ui.localgallery.GalleryActivity
import com.arkadiusz.dayscounter.util.*
import com.arkadiusz.dayscounter.util.DateUtils.formatDate
import com.arkadiusz.dayscounter.util.DateUtils.formatDateAccordingToSettings
import com.arkadiusz.dayscounter.util.DateUtils.formatTime
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.options
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.content_add.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor
import java.io.File
import java.util.*

@AndroidEntryPoint
abstract class BaseAddEditActivity : AppCompatActivity() {

    protected val viewModel: AddEditViewModel by viewModels()

    private val cropImage =
        registerForActivityResult(CropImageContract(CropActivity::class.java)) { result ->
            if (result.isSuccessful) {
                result.uriContent?.let { uri ->
                    viewModel.updateImageToLocalFile(StorageUtils.saveImage(this, uri).path!!)
                }
            }
        }

    private val localGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.updateImageToLocalGalleryItem(result.data?.getIntExtra("imageID", 0) ?: 0)
            }
        }

    private val internetGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUriString = result.data?.getStringExtra("internetImageUri") ?: ""
                if (imageUriString.isNotEmpty()) {
                    viewModel.updateImageToLocalFile(Uri.parse(imageUriString).path!!)
                }
            }
        }

    private val requestPermissionForInternetActivity =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                internetGallery.launch(Intent(this, InternetGalleryActivity::class.java))
            }
        }

    private val requestPermissionForPickingImage =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                cropImage.launch(
                    options {
                        setAspectRatio(18, 9)
                        setFixAspectRatio(true)
                    }
                )
            }
        }

    private var wasTimePickerAlreadyDisplayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(false, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_add)
        setUpSpinners()
        setUpCheckboxes()
        setUpEditTexts()
        setUpSeekBar()
        setUpFontColorPicker()
        setUpOnClickListeners()
        setUpImageChoosing()
        observeData()
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

        repeatSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.add_activity_repeat,
            R.layout.support_simple_spinner_dropdown_item
        )

        fontTypeSpinner.adapter = FontTypeSpinnerAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.font_type).toList()
        )

        setUpSpinnerListeners()
    }

    private fun setUpSpinnerListeners() {
        repeatSpinner.doOnSelected { _, position ->
            viewModel.updateRepetitionSelection(position)
        }

        counterFontSizeSpinner.doOnSelected { view, _ ->
            (view as? TextView)?.text?.let { text ->
                viewModel.updateCounterFontSize(text.toString().toInt())
            }
        }

        titleFontSizeSpinner.doOnSelected { view, _ ->
            (view as? TextView)?.text?.let { text ->
                viewModel.updateTitleFontSize(text.toString().toInt())
            }
        }

        fontTypeSpinner.doOnSelected { view, _ ->
            (view as? TextView)?.text?.let { text ->
                viewModel.updateFontType(text.toString())
            }
        }
    }

    private fun setUpCheckboxes() {
        showDividerCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateLineDividerSelection(isChecked)
        }

        yearsCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateFormatYearsSelection(isChecked)
        }

        monthsCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateFormatMonthsSelection(isChecked)
        }

        weeksCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateFormatWeeksSelection(isChecked)
        }

        daysCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateFormatDaysSelection(isChecked)
        }
    }

    private fun setUpEditTexts() {
        titleEditText.doAfterTextChanged { editable ->
            viewModel.updateName(editable?.toString() ?: "")
        }

        descriptionEditText.doAfterTextChanged { editable ->
            viewModel.updateDescription(editable?.toString() ?: "")
        }

        reminderTextEditText.doAfterTextChanged { editable ->
            viewModel.updateNotificationText(editable?.toString() ?: "")
        }
    }

    private fun setUpSeekBar() {
        pictureDimSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.updateDimValue(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // nop
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // nop
            }
        })
    }

    private fun setUpFontColorPicker() {
        colorImageView.setOnClickListener {
            displayFontColorPicker()
        }
    }

    private fun displayFontColorPicker() {
        ColorPickerDialogBuilder
            .with(this)
            .setTitle("Choose color")
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
            .density(10)
            .setPositiveButton("ok") { _, color, _ -> viewModel.updateFontColor(color) }
            .setNegativeButton("cancel") { _, _ -> }
            .also { if (viewModel.getSelectedFontColor() != -1) it.initialColor(viewModel.getSelectedFontColor()) }
            .build()
            .show()
    }

    private fun setUpOnClickListeners() {
        dateEditText.setOnClickListener { showDatePicker() }
        reminderDateEditText.setOnClickListener { showReminderDatePicker() }
        clearReminderDateButton.setOnClickListener { viewModel.clearReminder() }
        addButton.setOnClickListener { handleSaveClick() }
    }

    private fun showDatePicker() {
        val (currentYear, currentMonth, currentDay) =
            DateUtils.getElementsFromDate(viewModel.getCurrentDate())

        DatePickerDialog(
            this, { _, chosenYear, chosenMonth, chosenDay ->
                viewModel.updateDate(formatDate(chosenYear, chosenMonth, chosenDay))
            },
            currentYear,
            currentMonth - 1,
            currentDay
        ).show()
    }

    private fun showReminderDatePicker() {
        val calendar = Calendar.getInstance()
        val components = viewModel.getCurrentReminderComponents()

        wasTimePickerAlreadyDisplayed = false

        DatePickerDialog(
            this, { _, chosenYear, chosenMonth, chosenDay ->
                viewModel.updateReminderDateComponents(chosenYear, chosenMonth, chosenDay)

                if (!wasTimePickerAlreadyDisplayed) {
                    displayTimePickerDialog()
                }
            },
            if (components.year == 0) calendar.get(Calendar.YEAR) else components.year,
            if (components.year == 0) calendar.get(Calendar.MONTH) else components.month,
            if (components.year == 0) calendar.get(Calendar.DAY_OF_MONTH) else components.day
        ).show()
    }

    private fun displayTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val components = viewModel.getCurrentReminderComponents()

        wasTimePickerAlreadyDisplayed = true

        TimePickerDialog(
            this, { view, chosenHour, chosenMinute ->
                if (view.isShown) {
                    viewModel.updateReminderTimeComponents(chosenHour, chosenMinute)
                }
            },
            if (components.hour == 0) calendar.get(Calendar.HOUR_OF_DAY) else components.hour,
            if (components.hour == 0) calendar.get(Calendar.MINUTE) else components.minute,
            true
        ).show()
    }

    private fun setUpImageChoosing() {
        imageChooserButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_activity_dialog_title))
                .setItems(getImageChooserDialogOptions()) { _, which ->
                    when (which) {
                        0 -> askForPermissionAndDisplayCropActivity()
                        1 -> localGallery.launch(Intent(this, GalleryActivity::class.java))
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

    private fun getImageChooserDialogOptions() = listOf(
        getString(R.string.add_activity_dialog_option_custom),
        getString(R.string.add_activity_dialog_option_gallery),
        getString(R.string.add_activity_dialog_option_color),
        getString(R.string.add_activity_dialog_option_internet)
    ).toTypedArray()

    private fun askForPermissionAndDisplayCropActivity() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionForPickingImage.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            cropImage.launch(
                options {
                    setAspectRatio(18, 9)
                    setFixAspectRatio(true)
                }
            )
        }
    }

    private fun displayColorPickerForEventBackground() {
        ColorPickerDialogBuilder
            .with(this)
            .setTitle("Choose color")
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
            .density(10)
            .setPositiveButton("ok") { _, color, _ -> viewModel.updateImageToBackgroundColor(color) }
            .setNegativeButton("cancel") { _, _ -> }
            .also {
                if (viewModel.getCurrentImage() is ColorBackground) {
                    it.initialColor((viewModel.getCurrentImage() as ColorBackground).color)
                }
            }
            .build()
            .show()
    }

    private fun askForPermissionAndDisplayInternetImageActivity() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionForInternetActivity.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            internetGallery.launch(Intent(this, InternetGalleryActivity::class.java))
        }
    }

    private fun observeData() {
        observe(viewModel.nameLiveData) { name ->
            if (name != titleEditText.text.toString()) {
                titleEditText.setText(name)
            }
        }

        observe(viewModel.nameLiveData) { name ->
            eventTitle.text = name
        }

        observe(viewModel.dateLiveData) { date ->
            dateEditText.setText(
                formatDateAccordingToSettings(
                    date, PreferenceUtils.defaultPrefs(this)["dateFormat"] ?: ""
                )
            )
        }

        observe(viewModel.counterTextLiveData) { counterText ->
            eventCalculateText.text = counterText
        }

        observe(viewModel.descriptionLiveData) { description ->
            if (description != descriptionEditText.text.toString()) {
                descriptionEditText.setText(description)
            }
        }

        observe(viewModel.selectionLiveData) { selection ->
            updateSelectionStates(selection)
            updateFont(selection)
            updateImageDim(selection)
        }

        observe(viewModel.imageLiveData) { image ->
            when (image) {
                is ColorBackground -> {
                    eventImage.setImageDrawable(null)
                    eventImage.backgroundColor = image.color
                }
                is LocalGalleryImage -> {
                    Glide.with(this).load(image.resourceId).into(eventImage)
                }
                is LocalFile -> {
                    Glide.with(this).load(File(image.path)).into(eventImage)
                }
                is CloudFile -> {
                    Glide.with(this)
                        .load(FirebaseStorage.getInstance().getReference(image.cloudPath))
                        .into(eventImage)
                }
            }
        }

        observe(viewModel.reminderComponents) { components ->
            val text = if (viewModel.isReminderSet()) {
                val datePart = formatDateAccordingToSettings(
                    formatDate(components.year, components.month, components.day),
                    PreferenceUtils.defaultPrefs(this)["dateFormat"] ?: ""
                )
                val timePart = formatTime(components.hour, components.minute)

                "$datePart $timePart"
            } else {
                ""
            }

            reminderDateEditText.setText(text)
        }

        observe(viewModel.notificationTextLiveData) { notificationText ->
            if (notificationText != reminderTextEditText.text.toString()) {
                reminderTextEditText.setText(notificationText)
            }
        }
    }

    private fun updateSelectionStates(selection: UISelection) {
        repeatSpinner.setSelection(selection.repetitionSelection)

        yearsCheckbox.isChecked = selection.formatYearsSelected
        monthsCheckbox.isChecked = selection.formatMonthsSelected
        weeksCheckbox.isChecked = selection.formatWeeksSelected
        daysCheckbox.isChecked = selection.formatDaysSelected

        showDividerCheckbox.isChecked = selection.lineDividerSelected
        eventLine.isVisible = selection.lineDividerSelected

        counterFontSizeSpinner.setSelection(counterFontSizeSpinner.indexOfFirst { item ->
            item.toString() == selection.counterFontSize.toString()
        })
        titleFontSizeSpinner.setSelection(titleFontSizeSpinner.indexOfFirst { item ->
            item.toString() == selection.titleFontSize.toString()
        })
        fontTypeSpinner.setSelection(FontUtils.getFontPositionFor(selection.fontType))
    }

    private fun updateFont(selection: UISelection) {
        eventTitle.textColor = selection.fontColor
        eventCalculateText.textColor = selection.fontColor
        colorImageView.backgroundColor = selection.fontColor
        eventLine.backgroundColor = selection.fontColor

        eventCalculateText.setTextSize(
            TypedValue.COMPLEX_UNIT_DIP,
            selection.counterFontSize.toFloat()
        )
        eventTitle.setTextSize(
            TypedValue.COMPLEX_UNIT_DIP,
            selection.titleFontSize.toFloat()
        )

        val typeFace = FontUtils.getFontFor(selection.fontType, this)
        eventTitle.typeface = typeFace
        eventCalculateText.typeface = typeFace
    }

    private fun updateImageDim(selection: UISelection) {
        pictureDimSeekBar.progress = selection.dimValue
        eventImage.setColorFilter(Color.argb(255 / 17 * selection.dimValue, 0, 0, 0))
    }

    protected fun addReminder(eventToBeAdded: Event) {
        if (viewModel.isReminderSet()) {
            RemindersUtils.addNewReminder(this, eventToBeAdded)
        }
    }
}