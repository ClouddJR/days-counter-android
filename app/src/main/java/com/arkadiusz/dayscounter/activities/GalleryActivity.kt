package com.arkadiusz.dayscounter.activities

import PreferenceUtils.defaultPrefs
import PreferenceUtils.get
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.adapters.GalleryAdapter
import com.arkadiusz.dayscounter.model.RecyclerItemClickListener
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_gallery.*
import org.jetbrains.anko.startActivity

/**
 * Created by arkadiusz on 11.03.18
 */

class GalleryActivity : AppCompatActivity() {

    private lateinit var imagesList: IntArray
    private var activityType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        setUpActionBar()
        receiveActivityType()
        displayAd()
        setUpImagesList()
        setUpRecyclerView()
    }

    private fun setUpActionBar() {
        supportActionBar?.title = getString(R.string.gallery_activity_title)
    }

    private fun receiveActivityType() {
        activityType = intent.getStringExtra("activity")
    }

    private fun displayAd() {
        val prefs = defaultPrefs(this)
        val areAdsRemoved: Boolean? = prefs["ads"]
        if (areAdsRemoved != true) {
            adView2.loadAd(AdRequest.Builder().build())
        } else {
            adView2.visibility = View.GONE
        }
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
        galleryRV.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        galleryRV.adapter = galleryAdapter
        galleryRV.addOnItemTouchListener(RecyclerItemClickListener(this, galleryRV, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                sendImageToAddActivity(position)
            }

            override fun onItemLongClick(view: View?, position: Int) {
                //not used
            }
        }))
    }

    private fun sendImageToAddActivity(position: Int) {
        when (activityType) {
            "Add" -> startActivity<AddActivity>("imageID" to imagesList[position])
            "Edit" -> startActivity<EditActivity>("imageID" to imagesList[position])
        }
        finish()
    }
}

