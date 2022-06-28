package com.example.wrk.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Gym {
    private String name;
    private String address;
    private double rating;
    private double latitude;
    private double longitude;
    private Boolean isOpen;

    public Gym(JSONObject jsonObject) throws JSONException {
        this.name = jsonObject.getString("name");
        this.address = jsonObject.getString("vicinity");
        this.latitude = jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
        this.longitude = jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng");

        try {
            this.isOpen = jsonObject.getJSONObject("opening_hours").getBoolean("open_now");
        }
        catch (JSONException e) {
            isOpen = null;          // null if no value found
        }
        try {
            this.rating = jsonObject.getDouble("rating");
        }
        catch (JSONException e) {
            this.rating = -1;       // negative if no value found
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public double getRating() {
        return rating;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public Boolean isOpen() {
        return isOpen;
    }

    public static List<Gym> fromJsonArray(JSONArray gymJsonArray) throws JSONException {
        List<Gym> nearbyGyms = new ArrayList<>();
        for (int i = 0; i < gymJsonArray.length(); i++) {
            nearbyGyms.add(new Gym(gymJsonArray.getJSONObject(i)));
        }
        return nearbyGyms;
    }
}
