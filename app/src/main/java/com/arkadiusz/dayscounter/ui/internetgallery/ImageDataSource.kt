package com.arkadiusz.dayscounter.ui.internetgallery

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.arkadiusz.dayscounter.data.model.NetworkResult
import com.arkadiusz.dayscounter.data.model.Status
import com.arkadiusz.dayscounter.data.model.unsplash.Image
import com.arkadiusz.dayscounter.data.model.unsplash.ImagesResponse
import com.arkadiusz.dayscounter.data.remote.UnsplashService
import com.arkadiusz.dayscounter.util.MessageWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ImageDataSource(
    private val unsplashService: UnsplashService,
    private val queryString: String
) : ItemKeyedDataSource<Long, Image>() {

    val networkResult = MutableLiveData<NetworkResult>()
    val dialogStart = MutableLiveData<MessageWrapper<Boolean>>()
    val dialogEnd = MutableLiveData<MessageWrapper<Boolean>>()

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Image>) {
        //not used
    }

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Image>
    ) {
        networkResult.postValue(NetworkResult(Status.EMPTY))
        dialogStart.postValue(MessageWrapper(true))

        unsplashService.getPhotosForQuery(queryString, "1")
            .enqueue(object : Callback<ImagesResponse> {

                override fun onResponse(
                    call: Call<ImagesResponse>,
                    response: Response<ImagesResponse>
                ) {
                    response.body()?.let { imageResponse ->
                        val nextPage = getNextPageFromResponse(response)
                        imageResponse.imagesList.forEach {
                            it.nextPage = nextPage.toLong()
                        }
                        callback.onResult(imageResponse.imagesList)

                        if (imageResponse.imagesList.isNotEmpty()) {
                            networkResult.postValue(NetworkResult(Status.NOT_EMPTY))
                        }

                        dialogEnd.postValue(MessageWrapper(true))
                    }
                }

                override fun onFailure(call: Call<ImagesResponse>, t: Throwable) {
                    dialogEnd.postValue(MessageWrapper(true))
                }
            })
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Image>) {

        //loading only three pages maximum to save api calls
        if (params.key.toInt() <= 3 && params.key.toInt() != -1) {

            unsplashService.getPhotosForQuery(queryString, params.key.toString())
                .enqueue(object : Callback<ImagesResponse> {

                    override fun onResponse(
                        call: Call<ImagesResponse>,
                        response: Response<ImagesResponse>
                    ) {
                        response.body()?.let { imageResponse ->
                            val nextPage = getNextPageFromResponse(response)
                            imageResponse.imagesList.forEach {
                                it.nextPage = nextPage.toLong()
                            }
                            callback.onResult(imageResponse.imagesList)

                        }
                    }

                    override fun onFailure(call: Call<ImagesResponse>, t: Throwable) {
                    }
                })
        }
    }

    override fun getKey(item: Image) = item.nextPage


    private fun getNextPageFromResponse(response: Response<ImagesResponse>): Int {
        Log.d("responseHeaders", response.headers()["link"].toString())
        response.headers()["link"]?.let {
            val nextPageUrl = it.split(",").last().trim().split(";")[0]
                .removePrefix("<")
                .removeSuffix(">")
            Log.d("KeyUrl", nextPageUrl)
            val nextPageUri = Uri.parse(nextPageUrl)
            val nextPage = nextPageUri.getQueryParameter("page")
            nextPage?.let { page ->
                return page.toInt()
            }
        }

        //return -1 if there was no more images
        return -1
    }

    class Factory(
        private val unsplashService: UnsplashService,
        private val queryString: String
    ) : DataSource.Factory<Long, Image>() {

        val imageDataSourceLiveData = MutableLiveData<ImageDataSource>()

        override fun create(): DataSource<Long, Image> {
            val imageDataSource = ImageDataSource(unsplashService, queryString)
            imageDataSourceLiveData.postValue(imageDataSource)
            return imageDataSource
        }
    }
}
