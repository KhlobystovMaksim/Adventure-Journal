package com.harman.android.myprojectforinternship;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class UserLocalStore {

    public static final String SP_NAME = "userDetails";
    SharedPreferences userLocalDatabase;

    public UserLocalStore(Context context) {
        userLocalDatabase = context.getSharedPreferences(SP_NAME,0);
    }

    public void storeUserData(User user) {
        Gson gson = new Gson();
        String json = gson.toJson(user);
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString(user.email, json);
        spEditor.apply();
    }
    public User getLoggedInUser(String email) {
        String json = userLocalDatabase.getString(email, null);
        if(json != null){
            Gson gson = new Gson();
            return gson.fromJson(json,User.class);
        }
        return null;
    }
    public void setUserLoggedIn(boolean LoggedIn){
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("LoggedIn", LoggedIn);
        spEditor.apply();
    }
    public void clearUSerData(){
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.clear();
        spEditor.apply();
    }
    public boolean getUserLoginIn() {
        return userLocalDatabase.getBoolean("LoggedIn", false);
    }
}
