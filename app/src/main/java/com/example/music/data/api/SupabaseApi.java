package com.example.music.data.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseApi {
    @GET("storage/v1/s3/{bucket}")
    Call<ResponseBody> listObjects(
            @Path("bucket") String bucket,
            @Query("list-type") int listType);
}
