package com.niceattiregames.andrey.funweather;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Andrey on 19.03.2018.
 */

public class JSONService {
    public String url;
    private Activity activity;

    private RequestQueue mQueue;

    DataJSONCallback dataJSONCallback;

    public JSONService(Activity activity) {
        this.activity = activity;
        this.dataJSONCallback = (DataJSONCallback) activity;
    }

    public void jsonRequest(final String url, final String requestName, final Boolean isQuotes) {
        Log.d("JSON1", "onResponse: ");
        mQueue = Volley.newRequestQueue(activity);
        //url = "http://api.openweathermap.org/data/2.5/weather?lat=55.703161&lon=37.769485&lang=ru&units=metric&cnt=10&appid=74594e92ea7f917b823f7ad9a222f74a";
/*        JsonRequest request1 = new JsonRequest() {
            @Override
            protected Response parseNetworkResponse(NetworkResponse response) {
                return null;
            }

            @Override
            public int compareTo(@NonNull Object o) {
                return 0;
            }
        }*/
        if (!isQuotes) {
            JsonRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("JSON2", "onResponse: ");
                    dataJSONCallback.onJSONDataReceive(response, requestName);
                /*try {
                    //JSONArray jsonArray = response.getJSONArray("coord");
                    //JSONObject coord = response.getJSONObject("coord");
                    dataJSONCallback.onJSONDataReceive(response, requestName);
                    Log.d("JSON4", "onResponse: ");

                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("JSON5", "onResponse: ");
                    error.printStackTrace();
                    //jsonRequest(url, requestName);
                }
            });
            mQueue.add(request);

        } else {

            // Formulate the request and handle the response.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("JSON6.1", "onResponse: ");
                    dataJSONCallback.onJSONDataReceiveQuotes(response, requestName);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("JSON6.2", "onResponse: ");
                    error.printStackTrace();
                }
            });
            mQueue.add(stringRequest);
        }
    }
}
