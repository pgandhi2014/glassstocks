package com.intel.pg.glassstocks;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Created by pgandhi on 1/30/14.
 */
public class JSONFunctions {
    private static final String TAG = "JSONFunctions";

    public static JSONObject getJSONfromURL(String url){
        InputStream is = null;
        String result = "";
        JSONObject jArray = null;

        try{
            HttpParams httpParameters = new BasicHttpParams();
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            HttpPost httppost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        }catch(Exception e){
            Log.e(TAG, "Error in http connection "+e.toString());
        }

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            // trim out the non JSON parts (if any)
            int index = sb.indexOf("{\"ResultSet\"");
            if (index > 0) {
                sb.delete(0, index);
            }
            result=sb.toString();
        }catch(Exception e){
            Log.e(TAG, "Error converting result "+e.toString());
        }

        try{
            jArray = new JSONObject(result);
        }catch(JSONException e){
            Log.e(TAG, "Error parsing data to JSON "+e.toString());
        }
        return jArray;
    }
}