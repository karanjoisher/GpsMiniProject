package com.example.aakash.gpsminiproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Aakash on 27-04-2018.
 */
class RecordedData
{
    public LatLng latLng;
    public String time;
    public RecordedData()
    {
        latLng = new LatLng(0.0f, 0.0f);
    }
};


public class MapActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    private LocationManager manager;
    private LocationListener locationListener;

    public String trackingNumber;
    public String myNumber;

    public RecordedData mMyRecordedData;
    public RecordedData mTrackingRecordedData;

    public TextView mLocationUpdateInfo;

    //Gets a reference to the root of the database, in this case at "gpsminiproject"
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onStart()
    {
        super.onStart();

        //Get reference to the tracking number entry in the database.
        //For eg at /8108892920 if tracking number is 8108892920
        DatabaseReference trackingRef = mRootRef.child(trackingNumber);

        //These are callbacks which will be called when the entries in
        //firebase database get updated
        trackingRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                //If the number we want to track exists in database

                if(dataSnapshot.exists())
                {
                    //Whenever the tracking number's value changes in database,
                    //get its new value from the database, extract its gps coords,
                    //time and use geocoder to get the address name, then update the map
                    //to show the location and set the textView to see other readable information

                    String info = dataSnapshot.getValue(String.class);

                    //Split the value obtained from database by commas.
                    //This will create an array of strings where
                    //[0] = latitude coordinate.
                    //[1] = longitude coordinate.
                    //[2] = time when the location was recorded.

                    String infoFieldWise[] = info.split(",");
                    mTrackingRecordedData.latLng = new LatLng(Float.parseFloat(infoFieldWise[0]), Float
                            .parseFloat
                                    (infoFieldWise[1]));
                    if (infoFieldWise.length >= 3)
                    {
                        mTrackingRecordedData.time = infoFieldWise[2];
                    }
                    mapUpdation();
                }
                else
                {
                    mLocationUpdateInfo.setText("The tracking number you specified is not " +
                            "registered.\n");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

    }


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Intent intent = getIntent();

        //Structs that will hold : latLng and time
        mMyRecordedData = new RecordedData();
        mTrackingRecordedData = new RecordedData();

        //Get numbers from MainActivity.
        myNumber = intent.getStringExtra("MY_NUMBER");
        trackingNumber = intent.getStringExtra("TRACKING_NUMBER");
        Log.d("NUMBERS", "my number: " + myNumber);
        Log.d("NUMBERS", "tracking number: " + trackingNumber);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mLocationUpdateInfo = (TextView) findViewById(R.id.locationUpdateInfo);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //get the location service
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        locationListener = new LocationListener()
        {
            @SuppressLint("MissingPermission")
            @Override
            public void onLocationChanged(Location location)
            {
                //Whenever this device's location is changed,
                //create a string that will contain latlng, time
                //then update the myNumber's entry in the database.

                if (location != null)
                {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    LatLng latLng = new LatLng(latitude, longitude);
                    DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date(location.getTime());
                    String formatted = format.format(date);

                    mMyRecordedData.latLng = latLng;
                    mMyRecordedData.time = formatted;

                    DatabaseReference myRef = mRootRef.child(myNumber);
                    myRef.setValue(mMyRecordedData.latLng.latitude + "," + mMyRecordedData.latLng
                            .longitude + "," + mMyRecordedData.time);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle)
            {

            }

            @Override
            public void onProviderEnabled(String s)
            {

            }

            @Override
            public void onProviderDisabled(String s)
            {

            }


        };
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    public void mapUpdation()
    {

        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mTrackingRecordedData.latLng, 15));
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(mTrackingRecordedData.latLng);
        circleOptions.radius(14.0f);
        circleOptions.fillColor(0xff2196f3 );
        circleOptions.strokeWidth(3.0f);
        circleOptions.strokeColor(0xff2196f3 );
        circleOptions.zIndex(Float.MAX_VALUE);
        mMap.addCircle(circleOptions);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }

        //Using geocoder to convert GPS coordinates to human readable address.
        Geocoder myLocation = new Geocoder(MapActivity.this, Locale.getDefault());
        List<Address> myList = null;
        try
        {
            myList = myLocation.getFromLocation(mTrackingRecordedData.latLng.latitude,mTrackingRecordedData.latLng.longitude, 1);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        String addressStr = "";
        if(myList != null)
        {
            Address address = (Address) myList.get(0);
            int max = 3;

            for(int i = 0; i < max; i++)
            {
                if (address.getAddressLine(i) != null && address.getAddressLine(i).length() != 0)
                {
                    addressStr += address.getAddressLine(i);
                    if(i != max - 1)
                    {
                        addressStr += ", ";
                    }
                }
            }
        }

        String message = "Tracking: " + trackingNumber + "\n";
        if(addressStr.length() != 0)
        {
            message += "Location: " + addressStr + "\n";
        }
        message += "Coordinates: (" +
                mTrackingRecordedData
                        .latLng
                        .latitude +
                ", "
                + mTrackingRecordedData.latLng.longitude +
                ")\n";
        message += "Recorded On: " + mTrackingRecordedData.time + "\n";
        mLocationUpdateInfo.setText(message);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //Sometimes we can't get location updates as GPS cannot locate the device.
        //In such cases we try to get last known location from these 3 sources:

        Location gpsLastKnown = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location networkLastKnown = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location passiveLastKnown = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        Location lastKnownLoc = null;
        if(gpsLastKnown != null)
        {
            lastKnownLoc = gpsLastKnown;
            Log.d("LASTKNOWN", "GPS USED");
        }
        else if(networkLastKnown != null)
        {
            lastKnownLoc = networkLastKnown;
            Log.d("LASTKNOWN", "NETWORK USED");
        }
        else if(passiveLastKnown != null)
        {
            lastKnownLoc = passiveLastKnown;
            Log.d("LASTKNOWN", "PASSIVE USED");
        }
        else
        {
            lastKnownLoc = null;
            Log.d("LASTKNOWN", "FAILURE");
        }

        if(lastKnownLoc != null)
        {

            //If we have a lastknown location then update the myNumber's entry in the database.

            Log.d("LASTKNOWN_TRUE", lastKnownLoc.toString());
            LatLng latLng = new LatLng(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date(lastKnownLoc.getTime());
            String formatted = format.format(date);
            mMyRecordedData.latLng = latLng;
            mMyRecordedData.time = formatted;
            DatabaseReference myRef = mRootRef.child(myNumber);
            myRef.setValue(mMyRecordedData.latLng.latitude + "," + mMyRecordedData.latLng
                    .longitude + "," + mMyRecordedData.time);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i("onPause...","paused");
    }
}