package com.arkadiusz.dayscounter.ui.internetgallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.arkadiusz.dayscounter.data.model.NetworkResult
import com.arkadiusz.dayscounter.data.model.unsplash.Image
import com.arkadiusz.dayscounter.data.remote.UnsplashService
import com.arkadiusz.dayscounter.util.MessageWrapper
import com.arkadiusz.dayscounter.util.StorageUtils.toFile
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

class InternetGalleryActivityViewModel : ViewModel() {

    lateinit var imagesDownloaded: LiveData<PagedList<Image>>
    val imageSaved = MutableLiveData<MessageWrapper<String>>()

    private val unsplashService: UnsplashService = UnsplashService.getService()

    private lateinit var sourceFactory: ImageDataSource.Factory
    private lateinit var savingImageDisposable: Disposable


    fun getPhotos(queryString: String) {
        sourceFactory = ImageDataSource.Factory(unsplashService, queryString)
        val config = PagedList.Config.Builder()
            .setPageSize(30)
            .setInitialLoadSizeHint(30)
            .build()
        imagesDownloaded = LivePagedListBuilder<Long, Image>(sourceFactory, config).build()
    }

    fun getNetworkState(): LiveData<NetworkResult> = Transformations
        .switchMap(sourceFactory.imageDataSourceLiveData) { it.networkResult }

    fun getDialogStart(): LiveData<MessageWrapper<Boolean>> = Transformations
        .switchMap(sourceFactory.imageDataSourceLiveData) { it.dialogStart }


    fun getDialogEnd(): LiveData<MessageWrapper<Boolean>> = Transformations
        .switchMap(sourceFactory.imageDataSourceLiveData) { it.dialogEnd }


    fun saveImage(image: Image, cacheDir: File) {
        savingImageDisposable = downloadImage(image.imageUrls.regularUrl, image.imageId, cacheDir)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                imageSaved.value = MessageWrapper(image.imageId)
                triggerDownload(image)
            }
            .subscribe { }
    }

    private fun triggerDownload(image: Image) {

        unsplashService.triggerImageDownload(imageId = image.imageId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    //not used
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    //not used
                }
            })
    }


    private fun downloadImage(
        urlString: String,
        fileName: String,
        cacheDir: File
    ): Observable<URI> {
        return Observable.create { emitter ->
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            val inputStream = connection.inputStream
            inputStream.toFile(cacheDir.absolutePath + "/$fileName.png")
            emitter.onComplete()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (::savingImageDisposable.isInitialized && !savingImageDisposable.isDisposed) {
            savingImageDisposable.dispose()
        }
    }
}