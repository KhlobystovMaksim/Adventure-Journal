package com.harman.android.myprojectforinternship;

public class User {

    String name, email, password;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.name = "";
    }
}
