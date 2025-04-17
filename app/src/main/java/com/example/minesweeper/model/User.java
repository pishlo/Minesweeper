package com.example.minesweeper.model;

public class User {
    private String username;
    private String password;
    private float gems;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.gems = 100; // default gems when registering
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public float getGems() {
        return gems;
    }

    public void setGems(float gems) {
        this.gems = gems;
    }
}
