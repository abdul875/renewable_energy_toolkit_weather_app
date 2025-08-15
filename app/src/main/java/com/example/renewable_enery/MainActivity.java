package com.example.renewable_enery;

import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private EditText editTextLocation;
    private Button buttonGetWeather;
    private TextView textViewResult;
    private static final String API_KEY = "NWHFUVASRA2ZHRHXSJQYJ9VTB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        editTextLocation = findViewById(R.id.editTextLocation);
        buttonGetWeather = findViewById(R.id.buttonGetWeather);
        textViewResult = findViewById(R.id.textViewResult);

        // Set button click listener
        buttonGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = editTextLocation.getText().toString().trim();
                if (!location.isEmpty()) {
                    new WeatherTask().execute(location);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class WeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textViewResult.setText("Loading weather data...");
            buttonGetWeather.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... locations) {
            try {
                String location = URLEncoder.encode(locations[0], "UTF-8");
                String urlString = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" +
                        location + "/today?unitGroup=metric&key=" + API_KEY + "&contentType=json";

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else {
                    return "Error: HTTP " + responseCode;
                }
            } catch (Exception e) {
                Log.e("WeatherApp", "Error fetching weather data", e);
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            buttonGetWeather.setEnabled(true);

            if (result.startsWith("Error:")) {
                textViewResult.setText(result);
                Toast.makeText(MainActivity.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
            } else {
                displayWeatherData(result);
            }
        }
    }

    private void displayWeatherData(String jsonResponse) {
        try {
            JSONObject weatherData = new JSONObject(jsonResponse);
            StringBuilder displayText = new StringBuilder();

            // Location info
            displayText.append("📍 LOCATION INFORMATION\n");
            displayText.append("Address: ").append(weatherData.optString("resolvedAddress", "N/A")).append("\n");
            displayText.append("Timezone: ").append(weatherData.optString("timezone", "N/A")).append("\n");
            displayText.append("Latitude: ").append(weatherData.optDouble("latitude", 0.0)).append("\n");
            displayText.append("Longitude: ").append(weatherData.optDouble("longitude", 0.0)).append("\n\n");

            // Current conditions
            JSONObject currentConditions = weatherData.optJSONObject("currentConditions");
            if (currentConditions != null) {
                displayText.append("🌤️ CURRENT CONDITIONS\n");
                displayText.append("Temperature: ").append(currentConditions.optDouble("temp", 0.0)).append("°C\n");
                displayText.append("Feels Like: ").append(currentConditions.optDouble("feelslike", 0.0)).append("°C\n");
                displayText.append("Humidity: ").append(currentConditions.optDouble("humidity", 0.0)).append("%\n");
                displayText.append("Conditions: ").append(currentConditions.optString("conditions", "N/A")).append("\n");
                displayText.append("Wind Speed: ").append(currentConditions.optDouble("windspeed", 0.0)).append(" km/h\n");
                displayText.append("Wind Direction: ").append(currentConditions.optDouble("winddir", 0.0)).append("°\n");
                displayText.append("Pressure: ").append(currentConditions.optDouble("pressure", 0.0)).append(" mb\n");
                displayText.append("Visibility: ").append(currentConditions.optDouble("visibility", 0.0)).append(" km\n");
                displayText.append("UV Index: ").append(currentConditions.optDouble("uvindex", 0.0)).append("\n");
                displayText.append("Cloud Cover: ").append(currentConditions.optDouble("cloudcover", 0.0)).append("%\n\n");
            }

            // Today's forecast
            JSONArray days = weatherData.optJSONArray("days");
            if (days != null && days.length() > 0) {
                JSONObject today = days.getJSONObject(0);
                displayText.append("📅 TODAY'S FORECAST\n");
                displayText.append("Date: ").append(today.optString("datetime", "N/A")).append("\n");
                displayText.append("Max Temp: ").append(today.optDouble("tempmax", 0.0)).append("°C\n");
                displayText.append("Min Temp: ").append(today.optDouble("tempmin", 0.0)).append("°C\n");
                displayText.append("Description: ").append(today.optString("description", "N/A")).append("\n");
                displayText.append("Precipitation: ").append(today.optDouble("precip", 0.0)).append(" mm\n");
                displayText.append("Precipitation Probability: ").append(today.optDouble("precipprob", 0.0)).append("%\n");
                displayText.append("Sunrise: ").append(today.optString("sunrise", "N/A")).append("\n");
                displayText.append("Sunset: ").append(today.optString("sunset", "N/A")).append("\n");
                displayText.append("Moon Phase: ").append(today.optDouble("moonphase", 0.0)).append("\n\n");

                // Hourly data (first few hours)
                JSONArray hours = today.optJSONArray("hours");
                if (hours != null && hours.length() > 0) {
                    displayText.append("⏰ HOURLY FORECAST (Next 6 Hours)\n");
                    for (int i = 0; i < Math.min(6, hours.length()); i++) {
                        JSONObject hour = hours.getJSONObject(i);
                        displayText.append(hour.optString("datetime", "N/A")).append(" - ");
                        displayText.append(hour.optDouble("temp", 0.0)).append("°C, ");
                        displayText.append(hour.optString("conditions", "N/A")).append("\n");
                    }
                }
            }

            textViewResult.setText(displayText.toString());

        } catch (JSONException e) {
            Log.e("WeatherApp", "Error parsing JSON", e);
            textViewResult.setText("Error parsing weather data: " + e.getMessage());
        }
    }
}
