package com.example.finalassignment;

public class UsersClass {
    private String email;
    private String currentLocation;

    public UsersClass() {
    }

    public UsersClass(String email, String location) {
        this.email = email;
        this.currentLocation = location;
    }

    public String getEmail() {
        return email;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }
}
