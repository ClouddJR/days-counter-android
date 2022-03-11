package com.arkadiusz.dayscounter.ui.internetgallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.arkadiusz.dayscounter.data.model.unsplash.Image
import com.arkadiusz.dayscounter.data.remote.UnsplashService
import com.arkadiusz.dayscounter.util.MessageWrapper
import com.arkadiusz.dayscounter.util.StorageUtils.toFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class InternetGalleryActivityViewModel @Inject constructor(
    private val unsplashService: UnsplashService
) : ViewModel() {

    private val _savedImage = MutableLiveData<MessageWrapper<String>>()
    val savedImage: LiveData<MessageWrapper<String>> = _savedImage

    fun getPhotos(queryString: String): LiveData<PagingData<Image>> {
        return Pager(PagingConfig(pageSize = 30)) {
            ImageDataSource(unsplashService, queryString)
        }.liveData
    }

    fun saveImage(image: Image, cacheDir: File) {
        viewModelScope.launch {
            downloadImage(image.imageUrls.regularUrl, image.imageId, cacheDir)
            triggerDownload(image)
            _savedImage.value = MessageWrapper(image.imageId)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun downloadImage(
        urlString: String,
        fileName: String,
        cacheDir: File
    ) {
        withContext(Dispatchers.IO) {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            val inputStream = connection.inputStream
            inputStream.toFile(cacheDir.absolutePath + "/$fileName.png")
        }
    }

    private suspend fun triggerDownload(image: Image) {
        unsplashService.triggerImageDownload(imageId = image.imageId)
    }
}