package com.arkadiusz.dayscounter.activities

import PreferenceUtils.defaultPrefs
import PreferenceUtils.get
import PreferenceUtils.set
import android.accounts.AccountManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.adapters.ViewPagerAdapter
import com.arkadiusz.dayscounter.fragments.FutureFragment
import com.arkadiusz.dayscounter.fragments.PastFragment
import com.arkadiusz.dayscounter.purchaseutils.IabHelper
import com.arkadiusz.dayscounter.repositories.DatabaseRepository
import com.arkadiusz.dayscounter.repositories.FirebaseRepository
import com.arkadiusz.dayscounter.utils.PurchasesUtils
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.common.AccountPicker
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.email
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity(), FirebaseRepository.RefreshListener {

    private val databaseRepository = DatabaseRepository()
    private val firebaseRepository = FirebaseRepository()

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var prefs: SharedPreferences
    private lateinit var helper: IabHelper

    private val requestsCodeEmail = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logEvents()
        setUpPreferences()
        setUpToolbar()
        setUpViewPager()
        setUpFABClickListener()
        checkForPurchases()
        showChangelog()
        setRefreshListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_contact -> {
                email("arekchmura@gmail.com", "Days Counter app")
            }
            R.id.action_sort_date_desc -> {
                prefs["sort_type"] = "date_desc"
                refreshDataInFragments()
            }
            R.id.action_sort_date_asc -> {
                prefs["sort_type"] = "date_asc"
                refreshDataInFragments()
            }
            R.id.action_sort_order -> {
                prefs["sort_type"] = "date_order"
                refreshDataInFragments()
            }
            R.id.action_remove_ads -> {
                try {
                    helper.launchPurchaseFlow(this, "1", 10001,
                            PurchasesUtils.PurchaseFinishedListener, "")
                } catch (e: IabHelper.IabAsyncInProgressException) {
                    e.printStackTrace()
                }
            }
            R.id.action_syncing -> {
                if (!FirebaseRepository.isNetworkEnabled(this)) {
                    toast(getString(R.string.main_activity_sync_no_connection)).show()
                } else {
                    getEmailAddress()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun refreshFragments() {
        val selectedMail = prefs["firebase-email"] ?: ""
        toast(getString(R.string.main_activity_sync_first_time) + " $selectedMail").show()
        refreshDataInFragments()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestsCodeEmail && resultCode == RESULT_OK) {
            data?.extras?.let {
                val selectedMail = data.extras.getString(AccountManager.KEY_ACCOUNT_NAME)
                if (isMailTheSameAsPrevious(selectedMail)) {
                    displayWarningToast()
                    return
                }

                if (isMailDifferentFromPrevious(selectedMail)) {
                    firebaseRepository.deletePreviousMail(selectedMail)
                }

                setMailToSync(selectedMail)
                processFirebase(selectedMail)
            }
        }
    }

    private fun refreshDataInFragments() {
        (viewPagerAdapter.getItem(0) as FutureFragment).refreshData()
        (viewPagerAdapter.getItem(1) as PastFragment).refreshData()
    }

    private fun getEmailAddress() {
        try {
            val intent = AccountPicker.newChooseAccountIntent(null, null,
                    arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), true, null, null, null, null)
            startActivityForResult(intent, requestsCodeEmail)
        } catch (e: ActivityNotFoundException) {
            toast(getString(R.string.main_activity_sync_toast)).show()
        }
    }

    private fun isMailTheSameAsPrevious(selectedMail: String): Boolean {
        val previousMail = prefs["firebase-email"] ?: ""
        return previousMail == selectedMail
    }

    private fun displayWarningToast() {
        val previousMail = prefs["firebase-email"] ?: ""
        toast(getString(R.string.main_activity_sync_same_email) + " $previousMail").show()
    }

    private fun isMailDifferentFromPrevious(selectedMail: String): Boolean {
        val previousMail = prefs["firebase-email"] ?: ""
        return previousMail != "" && selectedMail != previousMail
    }

    private fun setMailToSync(selectedMail: String) {
        prefs["firebase-email"] = selectedMail
    }

    private fun processFirebase(selectedMail: String) {
        firebaseRepository.processSyncOperationFor(selectedMail)
    }

    private fun logEvents() {
        databaseRepository.logEvents()
    }

    private fun setUpPreferences() {
        prefs = defaultPrefs(this)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.app_name)
    }

    private fun setUpViewPager() {
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(FutureFragment(), getString(R.string.main_activity_right_tab))
        viewPagerAdapter.addFragment(PastFragment(), getString(R.string.main_activity_left_tab))
        viewPager.adapter = viewPagerAdapter
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                fab.show()
            }
        })
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun setUpFABClickListener() {
        fab.setOnClickListener {
            val eventType = if (viewPagerAdapter.getItem(viewPager.currentItem) is PastFragment) {
                "past"
            } else {
                "future"
            }
            startActivity<AddActivity>("Event Type" to eventType)
        }
    }

    private fun checkForPurchases() {
        helper = IabHelper(this, PurchasesUtils.base64EncodedPublicKey)
        PurchasesUtils.sharedPreferences = defaultPrefs(this)
        helper.startSetup { result ->
            if (!result.isSuccess) {
                // Problem
            } else {
                try {
                    helper.queryInventoryAsync(PurchasesUtils.GotInventoryListener)
                } catch (e: IabHelper.IabAsyncInProgressException) {
                    e.printStackTrace()
                }
            }
        }
        invalidateOptionsMenu()
    }

    private fun showChangelog() {
        if (!wasDialogSeenBefore()) {

            alert(getString(R.string.changelog_dialog_content)) {
                title = getString(R.string.changelog_dialog_title)
                positiveButton("OK") {}
                negativeButton(R.string.add_activity_back_button_cancel) {}
            }.show()

            prefs["dialog-seen-200"] = true
        }
    }

    private fun wasDialogSeenBefore() = prefs["dialog-seen-200"] ?: false

    private fun setRefreshListener() {
        firebaseRepository.setRefreshListener(this)
    }

}