package com.example.wrk;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {

    // Initializes parse SDK
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("UvPVrtPTjTAssjhNb38M4frI3TAB4W8sbNtCVFzl")
                .clientKey("2hAd20HEgVCNPptNkuCnRrWDDMSkk1D77llezJ5h")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
