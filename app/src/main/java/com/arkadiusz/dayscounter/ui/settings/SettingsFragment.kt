package com.arkadiusz.dayscounter.ui.settings

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.ui.premium.PremiumActivity
import com.arkadiusz.dayscounter.utils.PurchasesUtils.displayPremiumInfoDialog
import com.arkadiusz.dayscounter.utils.PurchasesUtils.isPremiumUser
import com.arkadiusz.dayscounter.utils.StorageUtils.BACKUP_PATH
import com.arkadiusz.dayscounter.utils.StorageUtils.isCorrectFileChosenForImport
import org.jetbrains.anko.browse
import org.jetbrains.anko.email
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity
import java.io.File


class SettingsFragment : PreferenceFragmentCompat() {

    private val databaseRepository = DatabaseRepository()

    private val REQUEST_EXPORT_DATA = 1
    private val REQUEST_IMPORT_DATA = 2

    private val REQUEST_FILE_CHOOSING = 3

    private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setUpRatePreference()
        setUpPremiumPreference()
        setUpBackupPreferences()
        setUpAboutPreferences()
        setUpThemesPreferences()
    }

    private fun setUpRatePreference() {
        val ratePreference = findPreference<Preference>("rate")
        ratePreference.setOnPreferenceClickListener {
            val appPackageName = context?.packageName // package name of the app
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (exception: android.content.ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
            true

        }
    }

    private fun setUpPremiumPreference() {
        val premiumPreference = findPreference<Preference>("premium")
        premiumPreference.setOnPreferenceClickListener {
            context?.startActivity<PremiumActivity>()
            true
        }
    }

    private fun setUpAboutPreferences() {
        val policyPreference = findPreference<Preference>("privacy_policy")
        policyPreference.setOnPreferenceClickListener {
            context?.browse("https://sites.google.com/view/dcprivacypolicy")
            true
        }

        val contactPreference = findPreference<Preference>("contact")
        contactPreference.setOnPreferenceClickListener {
            context?.email("arekchmura@gmail.com", "Days Counter app")
            true
        }
    }

    private fun setUpBackupPreferences() {
        val exportPreference = findPreference<Preference>("backup_export")
        exportPreference.setOnPreferenceClickListener {
            activity?.let { activity ->
                checkStoragePermissions(activity, true)
            }
            true
        }

        val importPreference = findPreference<Preference>("backup_import")
        importPreference.setOnPreferenceClickListener {
            activity?.let { activity ->
                checkStoragePermissions(activity, false)
            }
            true
        }
    }

    private fun setUpThemesPreferences() {
        val themesPreference = findPreference<Preference>("theme")

        if (!isPremiumUser(context)) {
            themesPreference.setOnPreferenceClickListener {
                displayPremiumInfoDialog(context)
                true
            }

        } else {
            themesPreference.setOnPreferenceChangeListener { _, _ ->
                activity?.recreate()
                true
            }
        }
    }


    private fun checkStoragePermissions(activity: Activity, exporting: Boolean) {
        val permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    PERMISSIONS_STORAGE,
                    if (exporting) REQUEST_EXPORT_DATA else REQUEST_IMPORT_DATA
            )
        } else {
            if (exporting) {
                backupData()
            } else {
                launchImportIntent()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_IMPORT_DATA -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    launchImportIntent()
                }
                return
            }

            REQUEST_EXPORT_DATA -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    backupData()
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_FILE_CHOOSING) {
                data?.data?.let {
                    importData(it)
                }
            }
        }
    }

    private fun backupData() {
        context?.let { ctx ->
            val backupPath = databaseRepository.backupData()
            ctx.longToast(getString(R.string.backup_saved_toast, backupPath))
        }
    }

    private fun launchImportIntent() {
        val intent = Intent()

        if (File(BACKUP_PATH).exists()) {
            val uri = Uri.parse(BACKUP_PATH)
            intent.setDataAndType(uri, "*/*")
        } else {
            intent.type = "*/*"
        }

        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_FILE_CHOOSING)
    }

    private fun importData(uri: Uri) {
        context?.let { ctx ->
            if (isCorrectFileChosenForImport(uri)) {
                databaseRepository.importData(ctx, uri)
            } else {
                ctx.longToast(getString(R.string.backup_toast_wrong_file))
            }
        }

    }
}
