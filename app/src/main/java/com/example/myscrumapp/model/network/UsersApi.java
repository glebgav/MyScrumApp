package com.example.myscrumapp.model.network;


import com.example.myscrumapp.model.entity.UserLoginDetails;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface UsersApi {
    @POST("users")
    Call<UserRegisterDetails> createUser(@Body UserRegisterDetails userRegisterDetails);

    @POST("login")
    Call<ResponseBody> userLogin(@Body UserLoginDetails userLoginDetails);


}
