package com.example.wrk;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.wrk.models.Gym;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.wrk.databinding.ActivityGymMapsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class GymMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityGymMapsBinding binding;
    private LocationManager locationManager;
    Location mCurrentLocation;
    private final static String KEY_LOCATION = "location";
    private double longitude;
    private double latitude;
    public String URL;
    private List<Gym> nearbyGyms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // show map on screen
        binding = ActivityGymMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        nearbyGyms = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(GymMapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(GymMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GymMapsActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15, 1, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();

                // Add a marker at user's current location
                LatLng currentCoords = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(currentCoords).title("My Current Location"));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentCoords, 14);   // changes how much to zoom in
                mMap.animateCamera(cameraUpdate);

                URL = String.format("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=5000&types=gym&key=%s", latitude, longitude, getString(R.string.MAPS_API_KEY));
                AsyncHttpClient client = new AsyncHttpClient();
                client.get(URL, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        JSONObject jsonObject = json.jsonObject;
                        try {
                            JSONArray results = jsonObject.getJSONArray("results");
                            nearbyGyms.addAll(Gym.fromJsonArray(results));      // get array of nearby gyms
                            // color of the marker icons
                            BitmapDescriptor openMarker =
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);   // currently open
                            BitmapDescriptor closedMarker =
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE);    // currently closed
                            BitmapDescriptor unsureMarker =
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);  // unsure if open/closed
                            // add markers for the nearby gyms
                            for (int i = 0; i < nearbyGyms.size(); i++) {
                                Gym gym = nearbyGyms.get(i);
                                double lat, lng;
                                lat = gym.getLatitude();
                                lng = gym.getLongitude();
                                LatLng gymLocation = new LatLng(lat, lng);
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .title(gym.getName())
                                        .snippet(String.valueOf(gym.getAddress()))
                                        .position(gymLocation);

                                if (gym.isOpen() != null && gym.isOpen()) {
                                    markerOptions.icon(openMarker);
                                }
                                else if (gym.isOpen() != null && !gym.isOpen()){
                                    markerOptions.icon(closedMarker);
                                }
                                else {
                                    markerOptions.icon(unsureMarker);       // isOpen is null if no open hours found from API
                                }
                                mMap.addMarker(markerOptions);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    }
                });
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}