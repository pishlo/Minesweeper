package com.example.minesweeper;

import com.example.minesweeper.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/api/users/register")
    Call<User> registerUser(@Body User user);

    @POST("/api/users/login")
    Call<User> loginUser(@Body User user);

    @POST("/api/users/update-gems")
    Call<User> updateGems(@Body User user);
}
