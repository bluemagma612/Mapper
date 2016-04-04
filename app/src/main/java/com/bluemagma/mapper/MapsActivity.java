package com.bluemagma.mapper;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.*;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    private final static String TAG = "MapsActivity";

    //location update intervals in seconds
    private final static int UPDATE_INTERVAL = 10000; //10 seconds
    private final static int FASTEST_INTERVAL = 5000; //5 seconds
    private final static int DISPLACEMENT = 5; //5 meters
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private GoogleMap mMap;

    //client to interact with google api
    private GoogleApiClient mGoogleApiClient;

    //boolean to enable/disable periodic location updates
    private boolean mRequestLocationUpdates = false;

    private Location mLastLocation;

    private LocationRequest mLocationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //build google api client object
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //create location request object
        createLocationRequest();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }


    //when connected attempt to get last location, move and zoom to it
    // and call location updates regardless
    @Override
    public void onConnected(Bundle connectionHint) {
        try {

            //log the attempt to get last location
            Log.i(TAG, "Getting last location...");

            //getting last location and setting it as the current location
            Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            //call show location passing the current location object
            showLocation(mCurrentLocation);
        }
        catch (SecurityException e){
            Log.e(TAG, "Cannot get last location", e);
            Toast.makeText(this, "Cannot get last location", Toast.LENGTH_SHORT).show();
        }

        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //resume location updates
        if (mGoogleApiClient.isConnected()) {

            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    //location request object creation
    protected void createLocationRequest () {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        catch (SecurityException e) {
            Log.e(TAG, "getLastLocation error", e);
        }
    }

    protected void enableMyLocation() {
        if (mMap != null) {
            try {
                Log.i(TAG, "setMyLocationEnabled = true");
                mMap.setMyLocationEnabled(true);
            }
            catch (SecurityException e) {
                Log.e(TAG, "setmyLocationEnabled error", e);
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    //move the camera and zoom in to the currently detected lat and lng
    protected void showLocation(Location mCurrentLocation) {
        if (mCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 15));
        }

        //draw a line from the last location and the current location



        if (mLastLocation != null) {
            mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                    .add(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));

            double currentLat =  mCurrentLocation.getLatitude();
            double currentLng = mCurrentLocation.getLongitude();
            double lastLat = mLastLocation.getLatitude();
            double lastLng = mLastLocation.getLongitude();

            Log.i(TAG, "Current lat " + Double.toString(currentLat));
            Log.i(TAG, "Last lat " + Double.toString(lastLat));
            Log.i(TAG, "Current lng " + Double.toString(currentLng));
            Log.i(TAG, "Last lng " + Double.toString(lastLng));
        }

        mLastLocation = mCurrentLocation;
    }

    protected void startLocationUpdates() {

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
        catch (SecurityException e){
            Log.e(TAG, "request location updates error", e);
        }
    }

    protected void stopLocationUpdates () {
        //stop location updates
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(), "Location Changed", Toast.LENGTH_SHORT).show();

        showLocation(location);

    }
}