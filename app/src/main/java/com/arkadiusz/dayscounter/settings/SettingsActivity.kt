package com.arkadiusz.dayscounter.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.activities.MainActivity
import org.jetbrains.anko.startActivity


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
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