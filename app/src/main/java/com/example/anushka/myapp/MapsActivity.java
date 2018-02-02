package com.example.anushka.myapp;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.anushka.myapp.model.Coordinates;
import com.example.anushka.myapp.volley.VolleySingleton;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.lang.reflect.Type;

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

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Coordinates> coordinates;
    int count = 0;
    private GPSTracker gpsTracker;
    private Location nLocation;
    double latitudes;
    double longtitudes;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        gpsTracker =new GPSTracker(getApplicationContext());
        nLocation =gpsTracker.getLocation();
        latitudes=nLocation.getLatitude();
        longtitudes=nLocation.getLongitude();
        coordinates = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        jsonRequest();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    Double gLat = 0.00;
    Double gLang = 0.00;


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMap(gLat,gLang);
        //getDeviceLocation();

    }
    List<android.location.Address>  addressList = null;

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
            LatLng destination = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(destination).title("Marker"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(destination));
          //  return address.getLatitude(),address.getLongitude();

        }
    }

    private void updateMap(double sourcelat,double sourcelong) {
        //android.location.Address address;
        LatLng barcelona = new LatLng(latitudes, longtitudes);
        mMap.addMarker(new MarkerOptions().position(barcelona));

        LatLng madrid = new LatLng(27.7172, 87.2460987);
        mMap.addMarker(new MarkerOptions().position(madrid));

        LatLng zaragoza = new LatLng(27.7172, 85.3240);

        //Define list to get all latlng for the route
        List<LatLng> path = new ArrayList();


        //Execute Directions API request
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(getResources().getString(R.string.google_maps_key))
                .build();
        DirectionsApiRequest req = DirectionsApi.getDirections(context, barcelona.latitude + "," + barcelona.longitude, madrid.latitude + "," + madrid.longitude);

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

        if (count >= 500) {
            //Draw the polyline
            if (path.size() > 0) {
                PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.RED).width(5);
                mMap.addPolyline(opts);
            }
        } else if(count >=250 && count<500) {
            if (path.size() > 0) {
                PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.YELLOW).width(5);
                mMap.addPolyline(opts);
            }
        }
        else {
            if (path.size() > 0) {
                PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.GREEN).width(5);
                mMap.addPolyline(opts);
            }
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zaragoza, 10f));
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
                        coordinates = gson.fromJson(jsonOutput, listType);
                        gLat = coordinates.get(0).getLat();
                        gLang = coordinates.get(0).getLang();
                        updateMap(gLat, gLang);
                        count = coordinates.get(0).getVechicleCount();


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        VolleySingleton.getInstance().getQueue().add(req);
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }


}
