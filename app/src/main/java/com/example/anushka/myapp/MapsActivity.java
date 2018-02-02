package com.example.anushka.myapp;

import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.anushka.myapp.model.Coordinates;
import com.example.anushka.myapp.volley.VolleySingleton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import org.json.JSONArray;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    int count = 100;
    private GPSTracker gpsTracker;
    private Location nLocation;
    double sourcelatitudes;
    double sourcelongitudes;
    double destinationLatitudes;
    double destinationLongitudes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        gpsTracker = new GPSTracker(getApplicationContext());
        nLocation = gpsTracker.getLocation();
        sourcelatitudes = nLocation.getLatitude();
        sourcelongitudes = nLocation.getLongitude();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMap();

    }

    List<android.location.Address> addressList = null;

    public void onMapSearch(View view) {
        EditText locationSearch = (EditText) findViewById(R.id.editText);
        String location = locationSearch.getText().toString();

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            android.location.Address address = addressList.get(0);
            destinationLatitudes = address.getLatitude();
            destinationLongitudes = address.getLongitude();
            destination = new LatLng(destinationLatitudes, destinationLongitudes);
            mMap.addMarker(new MarkerOptions().position(destination));
        }
        drawRoute();
    }

    LatLng source;
    LatLng destination;

    private void updateMap() {
        //android.location.Address address;
        source = new LatLng(sourcelatitudes, sourcelongitudes);
        mMap.addMarker(new MarkerOptions().position(source));
    }

    private void drawRoute() {
        List<LatLng> path = new ArrayList();

        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(getResources().getString(R.string.google_maps_key))
                .build();

//        String response = getDirectionsUrl(source, destination);
        DirectionsApiRequest req = DirectionsApi.getDirections(context, source.latitude + "," + source.longitude, destination.latitude + "," + destination.longitude);

        try {
            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs != null) {
                    for (int i = 0; i < route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j = 0; j < leg.steps.length; j++) {
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length > 0) {
                                    for (int k = 0; k < step.steps.length; k++) {
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("map", ex.getLocalizedMessage());
        }


        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.RED).width(5);
            mMap.addPolyline(opts);
        }


        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 18));

    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        //Adding Alternative parameter
        String alternative = "alternatives=true";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + alternative;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private void jsonRequest() {

        JsonArrayRequest req = new JsonArrayRequest("https://raw.githubusercontent.com/shanushka/Test/master/latlong.json",
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        String jsonOutput = response.toString();
                        Type listType = new TypeToken<ArrayList<Coordinates>>() {
                        }.getType();

//                        updateMap(gLat, gLang);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        VolleySingleton.getInstance().getQueue().add(req);
    }
}
