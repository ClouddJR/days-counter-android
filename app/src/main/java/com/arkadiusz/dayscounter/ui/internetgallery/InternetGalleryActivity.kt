package com.arkadiusz.dayscounter.ui.internetgallery

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.Status
import com.arkadiusz.dayscounter.data.model.unsplash.Image
import com.arkadiusz.dayscounter.ui.addeditevent.AddActivity
import com.arkadiusz.dayscounter.ui.addeditevent.EditActivity
import com.arkadiusz.dayscounter.utils.StorageUtils.saveFile
import com.arkadiusz.dayscounter.utils.ThemeUtils
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_internet_gallery.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.startActivity
import java.io.File


class InternetGalleryActivity : AppCompatActivity() {

    private lateinit var viewModel: InternetGalleryActivityViewModel

    private lateinit var activityType: String

    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(true, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internet_gallery)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        activityType = intent.getStringExtra("activity")
        setUpSearchView()
        setUpViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        cacheDir.deleteRecursively()
    }


    private fun setUpViewModel() {
        viewModel = ViewModelProviders.of(this).get(InternetGalleryActivityViewModel::class.java)
    }

    private fun setUpSearchView() {
        searchQuery.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                getPhotosAndObserveResult()
                hideKeyboard(this@InternetGalleryActivity)
                searchQuery.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun getPhotosAndObserveResult() {
        viewModel.getPhotos(searchQuery.query.toString())

        viewModel.imagesDownloaded.observe(this, Observer {
            setUpRecyclerView(it)
        })

        viewModel.imageSaved.observe(this, Observer {
            it.get()?.let { fileName ->
                progressDialog.cancel()
                startCropImageActivity(Uri.fromFile(File(cacheDir.absolutePath + "/$fileName.png")))
            }
        })

        viewModel.getNetworkState().observe(this, Observer {

            when (it.status) {
                Status.NOT_EMPTY -> {
                    noResultsTV.visibility = View.GONE
                    imagesRV.visibility = View.VISIBLE
                }

                Status.EMPTY -> {
                    noResultsTV.visibility = View.VISIBLE
                    imagesRV.visibility = View.GONE
                }
            }
        })

        lateinit var dialog: AlertDialog
        viewModel.getDialogStart().observe(this, Observer {
            dialog = indeterminateProgressDialog(message = getString(R.string.dialog_wait_prompt),
                    title = getString(R.string.dialog_downloading))
        })

        viewModel.getDialogEnd().observe(this, Observer {
            dialog.cancel()
        })
    }

    private fun startCropImageActivity(imageUri: Uri) {
        CropImage.activity(imageUri)
                .setAspectRatio(18, 9)
                .setFixAspectRatio(true)
                .start(this)
    }

    private fun returnToActivity(fileName: String?) {
        when (activityType) {
            "Add" -> startActivity<AddActivity>("internetImageUri" to fileName)
            "Edit" -> startActivity<EditActivity>("internetImageUri" to fileName)
        }
        finish()
    }

    private fun setUpRecyclerView(imagesList: PagedList<Image>) {

        val adapter = InternetGalleryAdapter(object : InternetGalleryAdapter.ImageClickListener {
            override fun onImageClick(image: Image) {
                progressDialog = indeterminateProgressDialog(message = getString(R.string.dialog_wait_prompt),
                        title = getString(R.string.dialog_saving))
                viewModel.saveImage(image, cacheDir)
            }
        })

        adapter.submitList(imagesList)

        imagesRV.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        imagesRV.adapter = adapter
    }


    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (isResultComingWithImageAfterCropping(requestCode)) {
            data?.let {
                var imageUri = CropImage.getActivityResult(data).uri as Uri
                imageUri = saveFile(this, imageUri)
                returnToActivity(imageUri.toString())
            }
        }

    }

    private fun isResultComingWithImageAfterCropping(requestCode: Int): Boolean {
        return requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
    }
}