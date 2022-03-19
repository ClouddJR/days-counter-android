package com.arkadiusz.dayscounter.data.remote

import com.arkadiusz.dayscounter.data.model.unsplash.ImagesResponse
import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UnsplashService {

    @GET("search/photos")
    suspend fun getPhotosForQuery(
        @Query("query") queryString: String,
        @Query("page") pageNumber: Int,
        @Query("client_id") clientId: String = Companion.clientId,
        @Query("per_page") imagesPerPage: Int = 30,
    ): Response<ImagesResponse>

    @GET("photos/{image}/download")
    suspend fun triggerImageDownload(
        @Path("image") imageId: String,
        @Query("client_id") clientId: String = Companion.clientId,
    ): Response<ResponseBody>

    companion object {
        private const val baseURL = "https://api.unsplash.com/"
        const val clientId = "dcdad029abf714214d392c5833737585362417d225d78b517c9f393db3309a49"

        fun getService(): UnsplashService {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return retrofit.create(UnsplashService::class.java)
        }
    }
}