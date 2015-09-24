package com.example.xingzhang.lecturefinder.Beacon;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by xingzhang on 04/09/2015.
 */
public class beaconParser extends lecture_activity {

    public static double updateDistance (Beacon beacon){
        int txpower = beacon.getMeasuredPower();
        double rssi = beacon.getRssi();
        return calculateAccuracy(txpower,rssi);
    }

    //converting signal strength rssi to meters
    public static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if cannot determine accuracy, return -1.
        }
        double accuracy ;
        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {

            return Math.pow(ratio,10);
        }
        else {
            accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }

    public static LatLng beaconLocation (Beacon beacon){

        LatLng beaconLocation = null;
        if (beacon.getMajor() == 51765 && beacon.getMinor() == 10653){
            beaconLocation = new LatLng(53.226448, -0.543725); //conference room
        }
        if (beacon.getMajor()== 20266 && beacon.getMinor() == 43427){
            beaconLocation = new LatLng(53.226579, -0.544044); //Entrance conner
        }
        if (beacon.getMajor() == 27540 && beacon.getMinor() == 19602){
            beaconLocation = new LatLng(53.226640, -0.544107); //Entrance
        }
        if (beacon.getMajor() == 12504 && beacon.getMinor() == 3035) {
            beaconLocation = new LatLng(53.226510, -0.543966); //lift
        }
        if (beacon.getMajor() == 49382 && beacon.getMinor() == 51183){
            beaconLocation = new LatLng(53.226614, -0.543926);//meeting room
        }
        if (beacon.getMajor() == 57120 && beacon.getMinor() == 22133){
            beaconLocation = new LatLng(53.226556, -0.543826);//destination
        }

        return beaconLocation;
    }

    //distinguish distance in different range
    public static LatLng detectLocation (Beacon beacon, double distance, Context context) {

        LatLng beaconLocation = beaconLocation(beacon);
        if (distance == -1) {
            Log.i(TAG, "no beacon detected!");
            return null;
        }
        if (distance < 1 && distance > 0) {
            //if distance within 1 meter, give beacon location
            Toast.makeText(context, "You have accessed the right room!", Toast.LENGTH_LONG).show();
            Log.i(TAG, "close: " + beaconLocation);
            return beaconLocation;
        } else {
            // far then 1 meter but within beacon signal strength
            LatLng farRange = farLocation(beaconLocation, distance);
            Log.i(TAG, "far: " + farRange);
            return farRange;
        }
    }

    //detect locations in a far range, using two points line algorithm
    public static LatLng farLocation(LatLng beaconLocation, double distance){

        double x1 = mapLocation.getLatitude();double y1 = mapLocation.getLongitude();
        double x2 = beaconLocation.latitude; double y2 = beaconLocation.longitude;
        double beaconGPS = pointsDistance(x1,y1,x2,y2); //distance between gps location to beacon
        double scope = distance / beaconGPS;
        double _x = scope*(x1-x2)+x2; double _y = scope*(y1-y2)+y2;
        return new LatLng(_x,_y);
    }

    public static final double EARTH_RADIUS = 6378137;

    //calculating the distance in meters by knowing two points geolocation
    public static double pointsDistance (double lat1, double lng1, double lat2, double lng2){

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return (EARTH_RADIUS * c);
    }
}