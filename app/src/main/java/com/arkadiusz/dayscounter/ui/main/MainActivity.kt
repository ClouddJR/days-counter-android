package com.arkadiusz.dayscounter.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.arkadiusz.dayscounter.DaysCounterApp
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.ui.addeditevent.AddActivity
import com.arkadiusz.dayscounter.ui.calculator.CalculatorActivity
import com.arkadiusz.dayscounter.ui.events.PastEventsFragment
import com.arkadiusz.dayscounter.ui.login.LoginActivity
import com.arkadiusz.dayscounter.ui.premium.PremiumActivity
import com.arkadiusz.dayscounter.ui.settings.SettingsActivity
import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import com.arkadiusz.dayscounter.util.PreferenceUtils.set
import com.arkadiusz.dayscounter.util.PurchasesUtils.displayPremiumInfoDialog
import com.arkadiusz.dayscounter.util.PurchasesUtils.isPremiumUser
import com.arkadiusz.dayscounter.util.ThemeUtils.getThemeFromPreferences
import com.arkadiusz.dayscounter.util.ViewModelUtils.getViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromPreferences(false, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewModel()
        addBillingLifecycleObserver()
        setUpPreferences()
        setUpToolbar()
        setUpViewPager()
        setUpFABClickListener()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        hideRemoveButtonIfPurchased(menu)
        return true
    }

    private fun hideRemoveButtonIfPurchased(menu: Menu?) {
        if (isPremiumUser(this)) {
            menu?.removeItem(R.id.action_remove_ads)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_syncing -> {
                if (isPremiumUser(this)) {
                    if (!viewModel.isUserLoggedIn()) {
                        startActivity<LoginActivity>()
                        finish()
                    } else {
                        displaySignOutDialog()
                    }
                } else {
                    displayPremiumInfoDialog(this)
                }
            }

            R.id.action_calculator -> {
                startActivity<CalculatorActivity>()
            }

            R.id.action_remove_ads -> {
                startActivity<PremiumActivity>()
            }

            R.id.action_change_view -> {
                val isCompactView = prefs["is_compact_view", false] ?: false
                prefs["is_compact_view"] = !isCompactView
            }

            R.id.action_settings -> {
                startActivity<SettingsActivity>()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displaySignOutDialog() {
        alert(getString(R.string.main_activity_email, viewModel.getUserEmail())) {
            positiveButton(getString(R.string.main_activity_sign_out)) {
                viewModel.signOut()
            }

            negativeButton(getString(R.string.add_activity_back_button_cancel)) {
                it.dismiss()
            }
        }.show()
    }

    private fun initViewModel() {
        viewModel = getViewModel(this) {
            MainActivityViewModel(
                billingRepository = (application as DaysCounterApp).billingRepository
            )
        }
    }

    private fun addBillingLifecycleObserver() {
        lifecycle.addObserver(viewModel.getBillingLifecycleObserver())
    }

    private fun setUpPreferences() {
        prefs = defaultPrefs(this)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.app_name)
    }

    private fun setUpViewPager() {
        viewPagerAdapter = ViewPagerAdapter(
            this, supportFragmentManager,
            prefs["default_fragment"] ?: ""
        )
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
            val eventType =
                when (viewPagerAdapter.getItem(viewPager.currentItem) is PastEventsFragment) {
                    true -> "past"
                    false -> "future"
                }
            startActivity<AddActivity>("Event Type" to eventType)
        }
    }
}