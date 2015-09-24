package com.example.xingzhang.lecturefinder.Timetable;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xingzhang on 27/07/2015.
 */
public class HTMLParser {

    final String TAG = "HTMLParser";
    static InputStream htmlData = null;

    public String doPost (String url, String id){
        String result = "";

        try{

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            //Post data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);

            nameValuePairs.add(new BasicNameValuePair("Step", "3"));
            nameValuePairs.add(new BasicNameValuePair("Student", id));
            nameValuePairs.add(new BasicNameValuePair("FromWeek", "0"));
            nameValuePairs.add(new BasicNameValuePair("ToWeek", "50"));
            nameValuePairs.add(new BasicNameValuePair("FromDay", "1"));
            nameValuePairs.add(new BasicNameValuePair("ToDay", "5"));

            //encode Post data
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            //Make post request
            HttpResponse response = httpClient.execute(httpPost);
            Log.d("HTTP response:", response.toString());

            HttpEntity httpEntity = response.getEntity();
            htmlData = httpEntity.getContent();
        }

        catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        catch (ClientProtocolException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(htmlData, "UTF-8"), 8);
            StringBuilder builder = new StringBuilder();
            String line = null;
            while((line = reader.readLine())!= null){
                builder.append(line+"n");
            }
            htmlData.close();
            result = builder.toString();
        }
        catch (Exception e){
            Log.e(TAG, "Error converting results" + e.toString());
        }
        return result;
    }
}