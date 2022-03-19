package com.arkadiusz.dayscounter.ui.internetgallery

import android.net.Uri
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.arkadiusz.dayscounter.data.model.unsplash.Image
import com.arkadiusz.dayscounter.data.model.unsplash.ImagesResponse
import com.arkadiusz.dayscounter.data.remote.UnsplashService
import retrofit2.Response

class ImageDataSource(
    private val unsplashService: UnsplashService,
    private val queryString: String
) : PagingSource<Int, Image>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Image> {
        return try {
            val pageNumber = params.key ?: 1
            val response = unsplashService.getPhotosForQuery(queryString, pageNumber)
            LoadResult.Page(
                data = response.body()?.imagesList ?: listOf(),
                prevKey = null,
                nextKey = if (pageNumber <= 3) getNextPageFromResponse(response) else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Image>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    private fun getNextPageFromResponse(response: Response<ImagesResponse>): Int? {
        response.headers()["link"]?.let {
            val lastLink = it.split(",").last().trim()
            val lastRel = lastLink.split(";").last()

            if (lastRel.contains("next")) {
                val lastPageUrl = lastLink.split(";").first()
                    .removePrefix("<")
                    .removeSuffix(">")
                val nextPage = Uri.parse(lastPageUrl).getQueryParameter("page")
                nextPage?.let { page ->
                    return page.toInt()
                }
            }
        }
        return null
    }
}
