package com.arkadiusz.dayscounter.ui.localgallery

import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.ui.addeditevent.AddActivity
import com.arkadiusz.dayscounter.ui.addeditevent.EditActivity
import com.arkadiusz.dayscounter.ui.common.RecyclerItemClickListener
import com.arkadiusz.dayscounter.util.ThemeUtils
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_gallery.*
import org.jetbrains.anko.startActivity

class GalleryActivity : AppCompatActivity() {

    private lateinit var imagesList: IntArray
    private var activityType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(true, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        setUpActionBar()
        receiveActivityType()
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

    private fun receiveActivityType() {
        activityType = intent.getStringExtra("activity")!!
    }

    private fun setUpImagesList() {
        imagesList = IntArray(58)
        for (i in 0 until imagesList.size) {
            imagesList[i] = resources.getIdentifier("a" + (i + 1), "drawable", packageName)
        }
    }

    private fun setUpRecyclerView() {
        val galleryAdapter = GalleryAdapter(imagesList, this)
        galleryRV.setHasFixedSize(true)
        galleryRV.layoutManager = androidx.recyclerview.widget.StaggeredGridLayoutManager(
            2,
            androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
        )
        galleryRV.adapter = galleryAdapter
        galleryRV.addOnItemTouchListener(
            RecyclerItemClickListener(this, galleryRV,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        sendImageToAddActivity(position)
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                        //not used
                    }
                })
        )
    }

    private fun sendImageToAddActivity(position: Int) {
        when (activityType) {
            "Add" -> startActivity<AddActivity>("imageID" to imagesList[position])
            "Edit" -> startActivity<EditActivity>("imageID" to imagesList[position])
        }
        finish()
    }

    private fun displayAd() {
        val prefs = defaultPrefs(this)
        val areAdsRemoved: Boolean? = prefs["ads", false]
        if (areAdsRemoved != true) {
            adView.loadAd(AdRequest.Builder().build())
        } else {
            adView.visibility = View.GONE
        }
    }
}

