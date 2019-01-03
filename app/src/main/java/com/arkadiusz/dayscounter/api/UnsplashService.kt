package com.arkadiusz.dayscounter.api

import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface UnsplashService {

    companion object {
        const val baseURL = "https://api.unsplash.com/"
        const val clientId = "dcdad029abf714214d392c5833737585362417d225d78b517c9f393db3309a49"

        fun getService(): UnsplashService {
            val gson = GsonBuilder()
                    .setLenient()
                    .create()

            val retrofit = Retrofit.Builder()
                    .baseUrl(UnsplashService.baseURL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

            return retrofit.create(UnsplashService::class.java)
        }
    }

    @GET("search/photos")
    fun getPhotosForQuery(@Query("query") queryString: String,
                          @Query("page") pageNumber: String = "1",
                          @Query("client_id") clientId: String = UnsplashService.clientId,
                          @Query("per_page") imagesPerPage: String = "30"): Call<ImagesResponse>

    @GET("photos/{image}/download")
    fun triggerImageDownload(@Path("image") imageId: String,
                             @Query("client_id") clientId: String = UnsplashService.clientId): Call<ResponseBody>
}