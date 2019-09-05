package com.arkadiusz.dayscounter.ui.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.ui.main.MainActivity
import com.arkadiusz.dayscounter.utils.ThemeUtils.getThemeFromPreferences
import org.jetbrains.anko.startActivity


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromPreferences(true, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
    }

    override fun onBackPressed() {
        startActivity<MainActivity>()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                startActivity<MainActivity>()
                finish()
            }
        }
        return true
    }
}