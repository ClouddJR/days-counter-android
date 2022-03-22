package com.arkadiusz.dayscounter.ui.internetgallery

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.data.model.unsplash.Image
import com.arkadiusz.dayscounter.ui.crop.CropActivity
import com.arkadiusz.dayscounter.util.StorageUtils.saveImage
import com.arkadiusz.dayscounter.util.ThemeUtils
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.options
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_internet_gallery.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jetbrains.anko.indeterminateProgressDialog
import java.io.File

@AndroidEntryPoint
class InternetGalleryActivity : AppCompatActivity() {

    private val cropImage =
        registerForActivityResult(CropImageContract(CropActivity::class.java)) { result ->
            if (result.isSuccessful) {
                result.getUriFilePath(this)?.let { path ->
                    returnToActivity(saveImage(this, path).toString())
                }
            }
        }

    private val viewModel: InternetGalleryActivityViewModel by viewModels()

    private lateinit var adapter: InternetGalleryAdapter

    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.getThemeFromPreferences(true, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internet_gallery)

        actionBar?.setDisplayHomeAsUpEnabled(true)
        setupViewModel()
        setupAdapter()
        setupSearchView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cacheDir.deleteRecursively()
    }

    private fun setupViewModel() {
        viewModel.savedImage.observe(this) {
            it.get()?.let { fileName ->
                progressDialog.cancel()
                cropImage.launch(
                    options(Uri.fromFile(File(cacheDir.absolutePath + "/$fileName.png"))) {
                        setAspectRatio(18, 9)
                        setFixAspectRatio(true)
                    }
                )
            }
        }
    }

    private fun setupAdapter() {
        adapter = InternetGalleryAdapter(object : InternetGalleryAdapter.ImageClickListener {
            override fun onImageClick(image: Image) {
                progressDialog = indeterminateProgressDialog(
                    message = getString(R.string.dialog_wait_prompt),
                    title = getString(R.string.dialog_saving)
                )
                viewModel.saveImage(image, cacheDir)
            }
        })

        imagesRV.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        imagesRV.adapter = adapter

        lifecycleScope.launch {
            adapter.loadStateFlow
                .collect { loadState ->
                    updateUIBasedOnState(loadState)
                }
        }
    }

    private fun updateUIBasedOnState(loadState: CombinedLoadStates) {
        val isEmptyList = loadState.source.refresh is LoadState.NotLoading
                && loadState.append.endOfPaginationReached
                && adapter.itemCount == 0
        val errorWhileFetching = loadState.source.refresh is LoadState.Error
        val isLoading = loadState.source.refresh is LoadState.Loading

        message.isVisible = isEmptyList.or(errorWhileFetching)
        message.text = getString(
            if (isEmptyList) R.string.no_results else R.string.internet_gallery_no_connection
        )

        imagesRV.isVisible = !isLoading && !errorWhileFetching
        progressBar.isVisible = isLoading
    }

    private fun setupSearchView() {
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
        viewModel.getPhotos(searchQuery.query.toString()).observe(this) { images ->
            lifecycleScope.launch {
                adapter.submitData(images)
            }
        }
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun returnToActivity(fileName: String) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("internetImageUri", fileName)
        })
        finish()
    }
}