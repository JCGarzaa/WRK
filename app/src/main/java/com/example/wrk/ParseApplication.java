package com.example.wrk;

import android.app.Application;

import com.example.wrk.models.Exercise;
import com.example.wrk.models.WorkoutComponent;
import com.example.wrk.models.WorkoutPerformed;
import com.example.wrk.models.WorkoutTemplate;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    // Initializes parse SDK
    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Exercise.class);
        ParseObject.registerSubclass(WorkoutComponent.class);
        ParseObject.registerSubclass(WorkoutTemplate.class);
        ParseObject.registerSubclass(WorkoutPerformed.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("UvPVrtPTjTAssjhNb38M4frI3TAB4W8sbNtCVFzl")
                .clientKey("2hAd20HEgVCNPptNkuCnRrWDDMSkk1D77llezJ5h")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
