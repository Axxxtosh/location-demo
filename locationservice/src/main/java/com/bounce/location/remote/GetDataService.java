package com.bounce.location.remote;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface GetDataService {

    @FormUrlEncoded
    @POST("location")
    Call<List<Example>> getAll(@Field("location_list") String locationInfoList);
}