package com.arkadiusz.dayscounter.activities

import PreferenceUtils.defaultPrefs
import PreferenceUtils.get
import PreferenceUtils.set
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.adapters.ViewPagerAdapter
import com.arkadiusz.dayscounter.fragments.FutureFragment
import com.arkadiusz.dayscounter.fragments.PastFragment
import com.arkadiusz.dayscounter.purchaseutils.IabHelper
import com.arkadiusz.dayscounter.repositories.DatabaseRepository
import com.arkadiusz.dayscounter.utils.PurchasesUtils
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.email
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    private val databaseRepository = DatabaseRepository()

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var prefs: SharedPreferences
    private lateinit var helper: IabHelper

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
                (viewPagerAdapter.getItem(0) as FutureFragment).refreshData()
                (viewPagerAdapter.getItem(1) as PastFragment).refreshData()
            }
            R.id.action_sort_date_asc -> {
                prefs["sort_type"] = "date_asc"
                (viewPagerAdapter.getItem(0) as FutureFragment).refreshData()
                (viewPagerAdapter.getItem(1) as PastFragment).refreshData()
            }
            R.id.action_sort_order -> {
                prefs["sort_type"] = "date_order"
                (viewPagerAdapter.getItem(0) as FutureFragment).refreshData()
                (viewPagerAdapter.getItem(1) as PastFragment).refreshData()
            }
            R.id.action_remove_ads -> {
                try {
                    helper.launchPurchaseFlow(this, "1", 10001,
                            PurchasesUtils.PurchaseFinishedListener, "")
                } catch (e: IabHelper.IabAsyncInProgressException) {
                    e.printStackTrace()
                }
            }
        }
        return super.onOptionsItemSelected(item)
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
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun setUpFABClickListener() {
        val eventType = if (viewPagerAdapter.getItem(viewPager.currentItem) is PastFragment) {
            "past"
        } else {
            "future"
        }
        fab.setOnClickListener {
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

            alert(getString(R.string.changelog_dialog_title), getString(R.string.changelog_dialog_content)) {
                positiveButton("OK") {}
                negativeButton(R.string.add_activity_back_button_cancel) {}
            }.show()

            prefs["dialog-seen-200"] = true
        }
    }

    private fun wasDialogSeenBefore() = prefs["dialog-seen-200"] ?: false

}