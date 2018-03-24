package com.niceattiregames.andrey.funweather;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jgabrielfreitas.core.BlurImageView;
import com.vansuita.gaussianblur.GaussianBlur;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements DataGPSCallback, DataJSONCallback {

    BlurImageView myBlurImage;
    GPSService gpsService;
    JSONService jsonService;
    Activity activity;

    TextView rayonTV;
    TextView coordinatesTextView;

    TextView humidityTV;
    TextView pressureTV;
    TextView minTempTV;
    TextView maxTempTV;

    TextView windSpeedTV;

    TextView pasmurnoTV;

    TextView temperatureTV;

    TextView jokeTV;



    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        /*            Blur effect                */
        myBlurImage = (BlurImageView) findViewById(R.id.blurImage);
        /*            Blur effect                */


        //++++++ variables
        rayonTV = (TextView) findViewById(R.id.rayon);
        coordinatesTextView = (TextView) findViewById(R.id.coordinates);
        humidityTV = (TextView) findViewById(R.id.humidity);
        pressureTV = (TextView) findViewById(R.id.pressure);
        minTempTV = (TextView) findViewById(R.id.minTemp);
        maxTempTV = (TextView) findViewById(R.id.maxTemp);
        windSpeedTV = (TextView) findViewById(R.id.windSpeed);
        pasmurnoTV = (TextView) findViewById(R.id.pasmurno);
        temperatureTV = (TextView) findViewById(R.id.temperature);
        jokeTV = (TextView) findViewById(R.id.joke);
        //------ variables

        updateDataOnScreen();

        gpsService = new GPSService(this);

        jsonService = new JSONService(this);
        jsonService.jsonRequest("http://rzhunemogu.ru/RandJSON.aspx?CType=1", "joke", true);
/*        if (timeIsComeToUpdateLocation()) {
            jsonService.jsonRequest("http://api.openweathermap.org/data/2.5/weather?lat=55.703161&lon=37.769485&lang=ru&units=metric&cnt=10&appid=appid");
        }*/

       isScreenBigerThen1080();

    }

    //++++++ Receive data Interface

    @Override
    public void onDataReceive(double lon, double lat) {
        coordinatesTextView.setText("Координаты: " + lat + ", " + lon + "  ");
        if (timeIsComeToUpdateLocation()) {
            jsonService.jsonRequest("http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&lang=ru&units=metric&cnt=10&appid=appid", "weather", false);
            saveTime();
        } else {
            /*SharedPreferences sharedPreferences = getSharedPreferences("WeatherData", Context.MODE_PRIVATE);
            //Date myDate = new Date(sharedPreferences.getLong("saveTime", 0));
            rayonTV.setText(sharedPreferences.getString("rayonTV", ""));
            humidityTV.setText(sharedPreferences.getString("humidityTV", ""));
            pressureTV.setText(sharedPreferences.getString("pressureTV", ""));
            minTempTV.setText(sharedPreferences.getString("minTempTV", ""));
            maxTempTV.setText(sharedPreferences.getString("maxTempTV", ""));
            windSpeedTV.setText(sharedPreferences.getString("windSpeedTV", ""));
            pasmurnoTV.setText(sharedPreferences.getString("pasmurnoTV", ""));
            temperatureTV.setText("");
            temperatureTV.setText(Html.fromHtml(getString(R.string.temperature,sharedPreferences.getString("temperatureTV", ""))));
            coordinatesTextView.setText(sharedPreferences.getString("coordinatesTextView", ""));*/
            updateDataOnScreen();
            //jsonService.jsonRequest("http://rzhunemogu.ru/RandJSON.aspx?CType=1", "joke", true);
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }

        //jsonService.jsonRequest("http://rzhunemogu.ru/RandJSON.aspx?CType=1", "joke", true);
    }

    @Override
    public void gpsDataTimeOut() {
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    @Override
    public void onJSONDataReceive(JSONObject response, String requestName) {
        Log.d("JSON3", "onResponse: ");
        if (requestName.equals("weather")) {
            try {
                JSONObject coord = response.getJSONObject("coord");
                JSONObject main = response.getJSONObject("main");

                JSONArray weather = response.getJSONArray("weather");
                JSONObject weatherDescArray = weather.getJSONObject(0);
                String weatherDesc = weatherDescArray.getString("description");

                JSONObject wind = response.getJSONObject("wind");

                // Round temperature to closest int
                double tempDouble = Double.parseDouble(main.getString("temp"));
                String tempString = String.valueOf(Math.round(tempDouble));

                // Change Alekseyevka to Kapotnia
                String kapotnia = response.getString("name");
                if (kapotnia.contains("Alekseyevka")) { kapotnia = "Kapotnya"; }

                rayonTV.setText(getString(R.string.rayon, kapotnia));
                humidityTV.setText(getString(R.string.humidity, main.getString("humidity")));
                pressureTV.setText(getString(R.string.pressure, main.getString("pressure")));
                minTempTV.setText(getString(R.string.min_temp, main.getString("temp_min")));
                maxTempTV.setText(getString(R.string.max_temp, main.getString("temp_max")));
                windSpeedTV.setText(getString(R.string.windSpeed, wind.getString("speed")));
                pasmurnoTV.setText(weatherDescArray.getString("description"));
                temperatureTV.setText(getString(R.string.temperature, tempString));
                // Change font
                temperatureTV.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));



                //save to shared preferences

                SharedPreferences sharedPreferences = getSharedPreferences("WeatherData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("rayonTV", rayonTV.getText().toString());
                editor.putString("humidityTV", humidityTV.getText().toString());
                editor.putString("pressureTV", pressureTV.getText().toString());
                editor.putString("minTempTV", minTempTV.getText().toString());
                editor.putString("maxTempTV", maxTempTV.getText().toString());
                editor.putString("windSpeedTV", windSpeedTV.getText().toString());
                editor.putString("pasmurnoTV", pasmurnoTV.getText().toString());
                editor.putString("temperatureTV", tempString);
                editor.putString("coordinatesTextView", coordinatesTextView.getText().toString());
                editor.commit();

                findViewById(R.id.loadingPanel).setVisibility(View.GONE);


                Log.d("JSON3.1", "onResponse: " + weatherDesc + " " + main.getString("temp") + " " + coord.getString("lon") + " " + coord.getString("lat"));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("JSON3.11", "onResponse: ");
            }
        } else if (requestName.equals("joke")) {
            String testString = (String) response.toString();
            Log.d("JSON4.1", "onResponse: " + testString);
            testString = testString.replace("{\"content\":\"", "");
            testString = testString.replace("\"}", "");
            testString = testString.replace("\"", "\'");
            Log.d("JSON4.2", "onResponse: " + testString);
            try {
                String content = response.getString("content");
                content = content.replace("\"", "\'");
                jokeTV.setText(content);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("JSON3.12", "onResponse: ");
            }
        }
        Log.d("weather json", "onJSONDataReceive: ");
    }

    @Override
    public void onJSONDataReceiveQuotes(String response, String requestName) {
        String testString = response;
        Log.d("JSON7.1", "onResponse: " + testString);
        testString = testString.replace("{\"content\":\"", "");
        testString = testString.replace("\"}", "");
        testString = testString.replace("\"", "\'");
        Log.d("JSON7.2", "onResponse: " + testString);
        jokeTV.setText(testString);

    }

    //------ Receive data Interface

    //++++++ Permission recieved

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 || requestCode == 102) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    gpsService.mLocationPermissionGranted = true;
                    //if (timeIsComeToUpdateLocation()) {
                        gpsService.getDeviceLocation();
                        //saveTime();
                    //}
                }
            } else {

                Toast.makeText(this,"permission denied",Toast.LENGTH_SHORT).show();

            }
            return;

        }
    }

    //------ Permission recieved

    public void getLocationButtonClick(View view) {
        if (timeIsComeToUpdateLocation()) {
            gpsService.getDeviceLocation();
            //saveTime();
        }
    }

    public void getNewJokeButtonClick(View view) {
        jsonService.jsonRequest("http://rzhunemogu.ru/RandJSON.aspx?CType=1", "joke", true);
    }

    private void saveTime() {
        Date date = new Date(); //or simply new Date();
        long millis = date.getTime();

        SharedPreferences sharedPreferences = getSharedPreferences("TimeData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("saveTime", millis);
        editor.commit();
        Log.d("JSON8.1", "onResponse: ");
    }

    private void loadTime() {

    }

    private Boolean timeIsComeToUpdateLocation() {
        Date date = new Date(); //or simply new Date();

        SharedPreferences sharedPreferences = getSharedPreferences("TimeData", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("saveTime")) {
            Date myDate = new Date(sharedPreferences.getLong("saveTime", 0));

            if (date.getTime() - myDate.getTime() >= 1 * 60 * 1000) {
                Log.d("JSON8.2", "onResponse: ");
                return true;
            }
            Log.d("JSON8.3", "onResponse: ");
            return false;
        } else {
            Log.d("JSON8.4", "onResponse: ");
            return true;
        }
    }

    private void updateDataOnScreen() {
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherData", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("rayonTV")) {
            //Date myDate = new Date(sharedPreferences.getLong("saveTime", 0));
            rayonTV.setText(sharedPreferences.getString("rayonTV", ""));
            humidityTV.setText(sharedPreferences.getString("humidityTV", ""));
            pressureTV.setText(sharedPreferences.getString("pressureTV", ""));
            minTempTV.setText(sharedPreferences.getString("minTempTV", ""));
            maxTempTV.setText(sharedPreferences.getString("maxTempTV", ""));
            windSpeedTV.setText(sharedPreferences.getString("windSpeedTV", ""));
            pasmurnoTV.setText(sharedPreferences.getString("pasmurnoTV", ""));
            temperatureTV.setText(getString(R.string.temperature, sharedPreferences.getString("temperatureTV", "")));
            coordinatesTextView.setText(sharedPreferences.getString("coordinatesTextView", ""));
        }
    }

    private void isScreenBigerThen1080() {
        Display display = this.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        boolean biger;

        if (width > 1090) {
            biger = true;
        } else {
            biger = false;
        }

        if (biger) {
            rayonTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);                 //20
            coordinatesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);     //13
            humidityTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,19);              //15
            pressureTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,19);              //15
            minTempTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,19);               //15
            maxTempTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,19);               //15
            windSpeedTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,19);             //15
            pasmurnoTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,19);              //15
            temperatureTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,160);          //125
            jokeTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,19);                  //15
            Log.d("isScreenBigerThen1080", "onResponse: ");
        }
    }
}

interface DataGPSCallback {
        public void onDataReceive(double lon, double lat);
        public void gpsDataTimeOut();
}

interface DataJSONCallback {
    public void onJSONDataReceive(JSONObject response, String requestName);
    public void onJSONDataReceiveQuotes(String response, String requestName);
}
