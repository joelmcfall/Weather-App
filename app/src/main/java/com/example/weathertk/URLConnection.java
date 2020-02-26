package com.example.weathertk;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class URLConnection extends AsyncTask<String, Void, String>
{
    @Override
    protected String doInBackground(String... urls)
    {
        String dlData = "";
        URL url;
        HttpURLConnection urlConnection;

        try
        {
            //stores the url
            url = new URL(urls[0]);

            urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);

            int data = reader.read();

            while(data != -1)
            {
                char current = (char) data;
                dlData += current;
                data = reader.read();
            }
            return dlData;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String dlData) {
        super.onPostExecute(dlData);

        try
        {
            //Splits the json
            JSONObject allData = new JSONObject(dlData);
            JSONObject mainData = new JSONObject(allData.getString("main"));
            JSONObject sysData = new JSONObject(allData.getString("sys"));
            JSONObject coordData = new JSONObject(allData.getString("coord"));

            //Stores the data we need
            String cityName = allData.getString("name");
            String countryCode = sysData.getString("country");

            double temp = Double.parseDouble(mainData.getString("temp"));
            double tempMin = Double.parseDouble(mainData.getString("temp_min"));
            double tempMax = Double.parseDouble(mainData.getString("temp_max"));
            double lat = Double.parseDouble(coordData.getString("lat"));
            double lon = Double.parseDouble(coordData.getString("lon"));
            int humidity = Integer.parseInt(mainData.getString("humidity"));
            int pressure = Integer.parseInt(mainData.getString("pressure"));
            int sunset = Integer.parseInt(sysData.getString("sunset"));
            int sunrise = Integer.parseInt(sysData.getString("sunrise"));

            //Sets the correct data to the Textviews in MainActivity
            MainActivity.cityName.setText(cityName);
            MainActivity.cityCountry.setText(countryCode);
            MainActivity.cityTemp.setText(String.valueOf(temp));
            MainActivity.cityMin.setText("Min temp: " + String.valueOf(tempMin));
            MainActivity.cityMax.setText("Max temp: " + String.valueOf(tempMax));
            MainActivity.cityLat.setText("Latitude: " + String.valueOf(lat));
            MainActivity.cityLon.setText("Longitude: " + String.valueOf(lon));
            MainActivity.cityHumid.setText("Humidity: " + String.valueOf(humidity));
            MainActivity.cityPress.setText("Pressure: " + String.valueOf(pressure));
            MainActivity.citySuns.setText("Sunset: " + DateConversion(sunset));
            MainActivity.citySunr.setText("Sunrise: " + DateConversion(sunrise));


        }
        catch (Exception e)
        {

        }
    }

    private String DateConversion (int unix)
    {
        String hours, mins, convertedDate;

        Date date = new java.util.Date(unix*1000L);

        hours = String.valueOf(date.getHours());
        mins = String.valueOf(date.getMinutes());

        if (date.getHours() > 12)
        {
            convertedDate = hours + ":" + mins + "PM";
            return convertedDate;
        }
        else
        {
            convertedDate = hours + ":" + mins + "AM";
            return convertedDate;
        }
    }
}
