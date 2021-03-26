package com.example.myscrumapp.model.network;

import com.example.myscrumapp.model.entity.User;
import com.example.myscrumapp.model.entity.UserLoginDetails;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import java.util.List;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface UsersApi {
    @POST("users")
    Call<UserRegisterDetails> createUser(@Body UserRegisterDetails userRegisterDetails);

    @POST("login")
    Call<ResponseBody> userLogin(@Body UserLoginDetails userLoginDetails);

    @GET("users")
    Single<List<User>> getAllUsers(@Header("authorization") String token, @Query("page")int page,@Query("limit") int limit);


}
