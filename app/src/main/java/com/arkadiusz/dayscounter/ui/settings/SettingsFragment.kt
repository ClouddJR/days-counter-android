package com.arkadiusz.dayscounter.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.repository.DatabaseRepository
import com.arkadiusz.dayscounter.ui.premium.PremiumActivity
import com.arkadiusz.dayscounter.util.PurchasesUtils.displayPremiumInfoDialog
import com.arkadiusz.dayscounter.util.PurchasesUtils.isPremiumUser
import com.arkadiusz.dayscounter.util.StorageUtils
import com.arkadiusz.dayscounter.util.StorageUtils.isCorrectFileChosenForImport
import dagger.hilt.android.AndroidEntryPoint
import org.jetbrains.anko.browse
import org.jetbrains.anko.email
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var databaseRepository: DatabaseRepository

    private val requestPermissionForImport =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                filePicker.launch(arrayOf())
            }
        }

    private val requestPermissionForBackup =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                backupData()
            }
        }

    private val filePicker =
        registerForActivityResult(object : ActivityResultContracts.OpenDocument() {
            override fun createIntent(context: Context, input: Array<String>): Intent {
                super.createIntent(context, input)
                return Intent(Intent.ACTION_GET_CONTENT).apply {
                    val backupPath = StorageUtils.getBackupPath(requireContext())
                    if (File(backupPath).exists()) {
                        setDataAndType(Uri.parse(backupPath), "*/*")
                    } else {
                        type = "*/*"
                    }
                }
            }
        }) { data -> data?.let { importData(it) } }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setUpRatePreference()
        setUpPremiumPreference()
        setUpBackupPreferences()
        setUpAboutPreferences()
        setUpThemesPreferences()
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseRepository.closeDatabase()
    }

    private fun setUpRatePreference() {
        val ratePreference = findPreference<Preference>("rate")
        ratePreference?.setOnPreferenceClickListener {
            val appPackageName = context?.packageName
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appPackageName")
                    )
                )
            } catch (exception: android.content.ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }
            true
        }
    }

    private fun setUpPremiumPreference() {
        val premiumPreference = findPreference<Preference>("premium")
        premiumPreference?.setOnPreferenceClickListener {
            context?.startActivity<PremiumActivity>()
            true
        }
    }

    private fun setUpAboutPreferences() {
        val policyPreference = findPreference<Preference>("privacy_policy")
        policyPreference?.setOnPreferenceClickListener {
            context?.browse("https://sites.google.com/view/dcprivacypolicy")
            true
        }

        val contactPreference = findPreference<Preference>("contact")
        contactPreference?.setOnPreferenceClickListener {
            context?.email("arekchmura@gmail.com", "Days Counter app")
            true
        }
    }

    private fun setUpBackupPreferences() {
        val importPreference = findPreference<Preference>("backup_import")
        importPreference?.setOnPreferenceClickListener {
            requestPermissionForImport.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            true
        }

        val exportPreference = findPreference<Preference>("backup_export")
        exportPreference?.setOnPreferenceClickListener {
            requestPermissionForBackup.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            true
        }
    }

    private fun setUpThemesPreferences() {
        val themesPreference = findPreference<Preference>("theme")

        if (!isPremiumUser(context)) {
            themesPreference?.setOnPreferenceClickListener {
                displayPremiumInfoDialog(context)
                true
            }
        } else {
            themesPreference?.setOnPreferenceChangeListener { _, _ ->
                activity?.recreate()
                true
            }
        }
    }

    private fun importData(uri: Uri) {
        if (isCorrectFileChosenForImport(uri)) {
            databaseRepository.importData(requireContext(), uri)
        } else {
            requireContext().longToast(getString(R.string.backup_toast_wrong_file))
        }
    }

    private fun backupData() {
        val backupPath = databaseRepository.backupData(requireContext())
        requireContext().longToast(getString(R.string.backup_saved_toast, backupPath))
    }
}
