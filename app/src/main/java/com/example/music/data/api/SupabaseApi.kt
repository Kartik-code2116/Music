package com.example.music.data.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SupabaseApi {
    @GET("storage/v1/s3/list/{bucket}")
    fun listObjects(
        @Path("bucket") bucket: String,
        @Query("max-keys") maxKeys: Int
    ): Call<ResponseBody>
}
