package com.arkadiusz.dayscounter.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arkadiusz.dayscounter.R


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
}