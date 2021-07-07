package com.tomykrisgreen.airbasetabbed;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable firebase offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // After enabling it, the data that is loaded will also be able to see offline
    }
}
