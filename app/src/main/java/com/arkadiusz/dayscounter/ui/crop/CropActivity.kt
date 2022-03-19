package com.arkadiusz.dayscounter.ui.crop

import android.os.Bundle
import com.arkadiusz.dayscounter.util.ThemeUtils
import com.canhub.cropper.CropImageActivity

class CropActivity : CropImageActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(true, this))
        super.onCreate(savedInstanceState)
    }
}