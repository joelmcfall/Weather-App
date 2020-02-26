package com.example.weathertk;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //Intent - passes search data to map
    public static final String LAT_MAP = "com.example.weathertk.LAT_MAP";
    public static final String LONG_MAP = "com.example.weathertk.LONG_MAP";
    public static final String NAME_MAP = "com.example.weathertk.NAME_MAP";

    //Vars for download and permissions
    private static final int REQUEST_FINE_LOCATION = 99;
    URLConnection task;

    //Vars xml
    static TextView cityName, cityCountry, cityTemp, cityLat, cityLon, cityHumid, cityPress, cityMin, cityMax, citySuns, citySunr;
    Button popUpBtn, mapBtn;

    //Pop up vars
    Dialog locationDia;
    EditText newCity, newCountry;
    ImageView closeDia;
    Button acceptDia, myLocDia;

    //Stores Users searched location or current location
    private double lat, lon;
    private String location, countryCode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inits the Text views to the correct data
        Configure();

        //Checks to see if the user has already given location permissions
        CheckLocationPerms();
    }

    private void Configure() {
        //Dialog
        locationDia = new Dialog(this);

        //Text
        cityName = findViewById(R.id.cityName);
        cityCountry = findViewById(R.id.cityCountry);
        cityTemp = findViewById(R.id.cityTemp);
        cityLat = findViewById(R.id.cityLat);
        cityLon = findViewById(R.id.cityLon);
        cityHumid = findViewById(R.id.cityHumid);
        cityPress = findViewById(R.id.cityPressure);
        cityMin = findViewById(R.id.cityMin);
        cityMax = findViewById(R.id.cityMax);
        citySunr = findViewById(R.id.sunrise);
        citySuns = findViewById(R.id.sunset);

        //Button
        popUpBtn = findViewById(R.id.locationPopUpButton);
        popUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowLocationPopUp();
            }
        });

        mapBtn = findViewById(R.id.viewOnMapbtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOnMap();
            }
        });
    }

    private void ShowOnMap() {
        //Removes the string to pass the double
        String lonString = cityLon.getText().toString();
        lonString = lonString.replace("Longitude: ", "");


        String latString = cityLat.getText().toString();
        latString = latString.replace("Latitude: ", "");

        //Sets the lon and lat variables to be what is currently showing
        lon = Double.parseDouble(lonString);
        lat = Double.parseDouble(latString);
        String mapName = cityName.getText().toString();

        //Sets up the lat and lon to be passed to the map
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(LAT_MAP, lat);
        intent.putExtra(LONG_MAP, lon);
        intent.putExtra(NAME_MAP, mapName);
        startActivity(intent);
    }

    private void ShowLocationPopUp()
    {
        locationDia.setContentView(R.layout.location_popup);

        //Edit text
        newCity = locationDia.findViewById(R.id.popCityName);
        newCountry = locationDia.findViewById(R.id.popCountryCode);

        //Image
        closeDia = locationDia.findViewById(R.id.closeDiag);

        //Button
        acceptDia = locationDia.findViewById(R.id.acceptDiag);
        myLocDia = locationDia.findViewById(R.id.myLocationDiag);

        //listener event for closing the pop up
        closeDia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationDia.dismiss();
            }
        });

        acceptDia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newCity != null)
                {
                    location = newCity.getText().toString();
                    countryCode = newCountry.getText().toString();
                    ExecuteTask(task, location, countryCode);
                    locationDia.dismiss();
                }
            }
        });

        myLocDia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetLocation();
                locationDia.dismiss();
            }
        });

        //sets the background to transparent
        locationDia.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        locationDia.show();
    }

    private void GetLocation() {
        //Creates a new instance as async can only run one task
        CancelTask();

        LocationManager lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = lManager.getBestProvider(new Criteria(), false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location currLocation = lManager.getLastKnownLocation(provider);

        //Stores the users last know lat and long
        lat = currLocation.getLatitude();
        lon = currLocation.getLongitude();

        //Execute task based on our location
        ExecuteTask(task, lat, lon);
    }

    private void CheckLocationPerms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //Permission not granted
            Log.d("LOCATION", "NO PERMISSIONS");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        }
        else
        {
            GetLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_FINE_LOCATION:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    GetLocation();
                }
                else
                {
                    //Error message saying location is needed for the app
                }
            }
        }
    }


    private void ExecuteTask(URLConnection task, String loc, String country)
    {
        //Creates a new instance as async can only run one task
        CancelTask();

        task = new URLConnection();

        if (country.matches(""))
        {
            task.execute(("https://api.openweathermap.org/data/2.5/weather?q=" + loc + "&units=metric&appid=764743cb48565cd99fbcedf75963a275"));
        }
        else
        {
            task.execute(("https://api.openweathermap.org/data/2.5/weather?q=" + loc + "," + country + "&units=metric&appid=764743cb48565cd99fbcedf75963a275"));
        }

    }

    private void ExecuteTask(URLConnection task, double latitude, double longitude)
    {
        task = new URLConnection();

        task.execute("https://api.openweathermap.org/data/2.5/weather?lat=" + String.valueOf(latitude) + "&lon=" + String.valueOf(longitude) + "&units=metric&appid=764743cb48565cd99fbcedf75963a275");
    }

    private void CancelTask() {
        if (task != null)
            task.cancel(true);
    }

}
