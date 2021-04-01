package com.example.myscrumapp.model.network;

import com.example.myscrumapp.model.entity.User;
import com.example.myscrumapp.model.entity.UserLoginDetails;
import com.example.myscrumapp.model.entity.UserRegisterDetails;
import java.util.List;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface UsersApi {
    @POST("users")
    Single<UserRegisterDetails> createUser(@Body UserRegisterDetails userRegisterDetails);

    @POST("login")
    Single<Response<ResponseBody>> userLogin(@Body UserLoginDetails userLoginDetails);

    @GET("users")
    Single<List<User>> getAllUsers(@Header("authorization") String token, @Query("page")int page,@Query("limit") int limit);

    @GET("users")
    Single<List<UserRegisterDetails>> getAllUsersWithTeamsAndTasks(@Header("authorization") String token, @Query("page")int page,@Query("limit") int limit);

    @DELETE("users/{userId}")
    Single<OperationResponseModel> deleteUser(@Header("authorization") String token,@Path("userId") String userId);

    @PUT("users/{userId}")
    Single<UserRegisterDetails> updateUser(@Header("authorization") String token, @Path("userId") String userId, @Body UserRegisterDetails user);


}
