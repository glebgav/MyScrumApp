package com.example.myscrumapp.model.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import retrofit2.HttpException;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OperationResponseModel {
    private String operationName;
    private String operationResult;
    private String responseMessage;


    public static OperationResponseModel successfulResponse(String operationName){
        return new OperationResponseModel(operationName, OperationResponseStatus.SUCCESS.name(), "");
    }

    public static OperationResponseModel failedResponse(String operationName, Throwable error){
        return new OperationResponseModel(operationName, OperationResponseStatus.ERROR.name(), getErrorMessage(error));
    }

    public static String getErrorMessage(Throwable err) {
        try {
            if(err instanceof java.net.SocketTimeoutException){
                return "timeout!";
            }
            HttpException error = (HttpException)err;
            String errorBody = error.response().errorBody().string();

            Map<String, String> retMap = new Gson().fromJson(
                    errorBody, new TypeToken<HashMap<String, String>>() {}.getType()
            );
            return  retMap.get("message");
        }catch (Exception e){
            return "";
        }

    }

}

