package com.arkadiusz.dayscounter.ui.localgallery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.ui.common.RecyclerItemClickListener
import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import com.arkadiusz.dayscounter.util.ThemeUtils
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_gallery.*

class GalleryActivity : AppCompatActivity() {

    private lateinit var images: IntArray

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(true, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        setUpActionBar()
        setUpImagesList()
        setUpRecyclerView()
        displayAd()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setUpActionBar() {
        supportActionBar?.title = getString(R.string.gallery_activity_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpImagesList() {
        images = IntArray(58)
        for (i in images.indices) {
            images[i] = resources.getIdentifier("a" + (i + 1), "drawable", packageName)
        }
    }

    private fun setUpRecyclerView() {
        galleryRV.setHasFixedSize(true)
        galleryRV.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        galleryRV.adapter = GalleryAdapter(images, this)

        galleryRV.addOnItemTouchListener(
            RecyclerItemClickListener(this, galleryRV,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        sendImageAsResult(position)
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                        //not used
                    }
                })
        )
    }

    private fun sendImageAsResult(position: Int) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("imageID", images[position])
        })
        finish()
    }

    private fun displayAd() {
        val areAdsRemoved: Boolean? = defaultPrefs(this)["ads", false]
        if (areAdsRemoved != true) {
            adView.loadAd(AdRequest.Builder().build())
        } else {
            adView.visibility = View.GONE
        }
    }
}

