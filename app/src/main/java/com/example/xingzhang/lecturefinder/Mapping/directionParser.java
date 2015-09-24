package com.example.xingzhang.lecturefinder.Mapping;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class directionParser extends AppCompatActivity {

    /**
     * Created by xingzhang on 17/08/2015.
     */
    GoogleMap mGoogleMap;

    public directionParser(GoogleMap myMap){
        this.mGoogleMap = myMap;
    }
    /*
   building the url for getting directionParser
   */
    public String getDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + ","
                + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
       // String sensor = "sensor=false";

        // Travelling Mode
        String mode = "mode=walking";

        //Direction server API key for indoors, very important!!!
        String key = "key=AIzaSyD1a7PQAA_XCV8SszGh8X38le24U-QRHiY";

        String parameters;
        // Building the parameters to the web service

        parameters = str_origin + "&" + str_dest  + "&"
                + mode + "&" + key;

        String output = "xml";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters;

        return url;
    }

    public class googleMapRouteTask extends
            AsyncTask<String, Void, List<LatLng>> {

        HttpClient client;
        String url;

        List<LatLng> routes = null;

        public googleMapRouteTask(String url) {
            this.url = url;
        }

        @Override
        protected List<LatLng> doInBackground(String... params) {

            HttpGet get = new HttpGet(url);

            try {
                HttpResponse response = client.execute(get);
                int statusecode = response.getStatusLine().getStatusCode();
                System.out.println("response:" + response + "      statuscode:"
                        + statusecode);
                if (statusecode == 200) {

                    String responseString = EntityUtils.toString(response
                            .getEntity());

                    int status = responseString.indexOf("<status>OK</status>");
                    System.out.println("status:" + status);
                    if (-1 != status) {
                        int pos = responseString.indexOf("<overview_polyline>");
                        pos = responseString.indexOf("<points>", pos + 1);
                        int pos2 = responseString.indexOf("</points>", pos);
                        responseString = responseString
                                .substring(pos + 8, pos2);
                        routes = decodePoly(responseString);
                    } else {
                        return null;
                    }

                } else {
                    //request fail
                    return null;
                }

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("doInBackground:"+routes);
            return routes;
        }

        @Override
        protected void onPreExecute() {
            client = new DefaultHttpClient();
            client.getParams().setParameter(
                    CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
                    15000);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<LatLng> routes) {
            super.onPostExecute(routes);
            if (routes == null) {
                // diection dail
                Toast.makeText(getApplicationContext(), "No available route found", Toast.LENGTH_LONG).show();
            }
            else{
                //draw on maps
                PolylineOptions lineOptions = new PolylineOptions();
                lineOptions.addAll(routes);
                lineOptions.width(3);
                lineOptions.color(Color.BLUE);
                mGoogleMap.addPolyline(lineOptions);
                //locate on 0 point
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(routes.get(0), 14.0f));
                //mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo());
            }
        }

    }
    public List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}
