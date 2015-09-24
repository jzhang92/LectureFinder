package com.example.xingzhang.lecturefinder.Beacon;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.example.xingzhang.lecturefinder.Mapping.directionParser;
import com.example.xingzhang.lecturefinder.Mapping.roomLocation;
import com.example.xingzhang.lecturefinder.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class lecture_activity extends AppCompatActivity {

    protected static final String TAG = "lecture";

    public String singleEvent;
    private GoogleMap mMap;
    protected static LatLng startPoint;
    protected static Location mapLocation;
    protected LatLng _location;
    protected static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);
    protected static BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture);

        singleEvent = getIntent().getExtras().getString("event");
        TextView module  = (TextView)findViewById(R.id.ModuleID);
        module.setText(singleEvent);

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                if (beacons != null) {
                    setupBeacon(beacons);
            }
        }
    });
        setUpMapIfNeeded();
    }

    private void setupBeacon(List<Beacon> beacons){
        ArrayList<Double> nearBeacon = new ArrayList<>();
        //select the nearest beacon
        for (int i = 0; i < beacons.size(); i++) {
            Beacon beacon = beacons.get(i);
            double distance = beaconParser.updateDistance(beacon);
            nearBeacon.add(i, distance);
        }

        int index = findMinIndex(nearBeacon);
        if (index != -1) {
            Beacon foundBeacon = beacons.get(index);
            LatLng thisBeacon = beaconParser.beaconLocation(foundBeacon);
            if (thisBeacon != null) {
                double distance = beaconParser.updateDistance(foundBeacon);
                _location = beaconParser.detectLocation(foundBeacon, distance, getApplicationContext());
                //Log.i(TAG, "update" + distance);
            }
        }
        else{
            _location = new LatLng(mapLocation.getLatitude(),mapLocation.getLongitude());
            Log.i(TAG, "map location: "+_location);
        }
    }

    public static <T extends Comparable<T>> int findMinIndex(final List<T> xs) {
        int minIndex;
        if (xs.isEmpty()) {
            minIndex = -1;
        } else {
            final ListIterator<T> itr = xs.listIterator();
            T min = itr.next();
            minIndex = itr.previousIndex();
            while (itr.hasNext()) {
                final T curr = itr.next();
                if (curr.compareTo(min) < 0) {
                    min = curr;
                    minIndex = itr.previousIndex();
                }
            }
        }
        return minIndex;
    }

    public void getRoom(View view){

        roomLocation rlocation = new roomLocation();
        String room = rlocation.roomCode(singleEvent);
        LatLng start;
        LatLng end = rlocation.codeLocation(room);

        if (_location != null){
            start = new LatLng(_location.latitude,_location.longitude);
        }
        else{
            start = startPoint;
        }

        directionParser direct = new directionParser(mMap);
        String url = direct.getDirectionsUrl(start, end);
        mMap.clear();
        directionParser.googleMapRouteTask task = direct.new googleMapRouteTask(url);
        task.execute();
        mMap.addMarker(new MarkerOptions()
                .position(end)
                .snippet("Lat: " + end.latitude + ", Lng: " + end.longitude)
                .title(room));
    }

    @Override
    protected void onStart(){
        super.onStart();

        if (!beaconManager.hasBluetooth()) {
            Toast.makeText(this, "Device does not have Bluetooth Low Energy sensor", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            connectToService();
        }
    }

    @Override
    protected void onDestroy() {
        beaconManager.disconnect();
        super.onDestroy();
    }

    @Override
    public void onStop(){
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
        } catch (RemoteException e) {
            Log.d(TAG, "Error while stopping ranging", e);
        }
        super.onStop();
    }

    private void connectToService() {
        //adapter.replaceWith(Collections.<Beacon>emptyList());
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
                } catch (RemoteException e) {
                    Toast.makeText(lecture_activity.this, "Cannot start ranging", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    protected void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        //due to the testing phone needs to choose network_provider, normally use getbestprovider
        String provider = locationManager.NETWORK_PROVIDER;

        LocationListener locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                showCurrentLocation(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };

        locationManager.requestLocationUpdates(provider, 2000, 0, locationListener);

        // Getting initial Location
        mapLocation = locationManager.getLastKnownLocation(provider);

        /* Show the initial location
            update with beacon data
         */
        if(mapLocation != null)
        {
            startPoint = new LatLng(mapLocation.getLatitude(), mapLocation.getLongitude());
            showCurrentLocation(mapLocation);
        }
        else {
            mMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(0, 0))
                            .title("707 Where am I?"));
        }
    }

    private void showCurrentLocation(Location location){

        if (_location == null) {
            LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot))
                    .position(currentPosition)
                    .snippet("Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude())
                    .title("I'm outdoor!"));

            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(currentPosition)
                    .zoom(17)
                    .bearing(0)
                    .build();
            // Zoom in, animating the camera.
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
        }
        else{
            LatLng currentPosition = new LatLng(_location.latitude, _location.longitude);


            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot))
                    .position(currentPosition)
                    .snippet("Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude())
                    .title("I'm indoor!"));

            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(currentPosition)
                    .zoom(19)
                    .bearing(0)
                    .build();
            // Zoom in, animating the camera.
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
        }
    }
}
