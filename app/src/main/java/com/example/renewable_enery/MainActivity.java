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
    private Button buttonEnergyCalculation;
    private TextView textViewResult;
    private static final String API_KEY = "NWHFUVASRA2ZHRHXSJQYJ9VTB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        editTextLocation = findViewById(R.id.editTextLocation);
        buttonGetWeather = findViewById(R.id.buttonGetWeather);
        buttonEnergyCalculation = findViewById(R.id.buttonEnergyCalculation);
        textViewResult = findViewById(R.id.textViewResult);

        // Set button click listeners
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

        buttonEnergyCalculation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = editTextLocation.getText().toString().trim();
                if (!location.isEmpty()) {
                    new EnergyCalculationTask().execute(location);
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

    private class EnergyCalculationTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textViewResult.setText("Calculating optimal energy source...");
            buttonEnergyCalculation.setEnabled(false);
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
            buttonEnergyCalculation.setEnabled(true);
            buttonGetWeather.setEnabled(true);

            if (result.startsWith("Error:")) {
                textViewResult.setText(result);
                Toast.makeText(MainActivity.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
            } else {
                calculateEnergySource(result);
            }
        }
    }

    private void calculateEnergySource(String jsonResponse) {
        try {
            JSONObject weatherData = new JSONObject(jsonResponse);
            StringBuilder displayText = new StringBuilder();

            // Extract weather parameters
            JSONObject currentConditions = weatherData.optJSONObject("currentConditions");
            JSONArray days = weatherData.optJSONArray("days");

            if (currentConditions == null || days == null || days.length() == 0) {
                textViewResult.setText("Error: Insufficient weather data for energy calculation");
                return;
            }

            JSONObject today = days.getJSONObject(0);

            // Extract data with defaults
            double solarRadiation = currentConditions.optDouble("solarradiation", 0.0);
            double uvIndex = currentConditions.optDouble("uvindex", 0.0);
            double cloudCover = currentConditions.optDouble("cloudcover", 0.0);
            double windSpeed = currentConditions.optDouble("windspeed", 0.0);
            double windGust = currentConditions.optDouble("windgust", windSpeed);
            double pressure = currentConditions.optDouble("pressure", 1013.25);
            double temp = currentConditions.optDouble("temp", 20.0);
            double humidity = currentConditions.optDouble("humidity", 50.0);
            double precip = today.optDouble("precip", 0.0);
            double precipProb = today.optDouble("precipprob", 0.0);

            // Calculate day length
            String sunriseStr = today.optString("sunrise", "06:00:00");
            String sunsetStr = today.optString("sunset", "18:00:00");
            double dayLengthMinutes = calculateDayLength(sunriseStr, sunsetStr);

            // Calculate energy scores
            double solarScore = calculateSolarScore(solarRadiation, uvIndex, cloudCover, dayLengthMinutes);
            double windScore = calculateWindScore(windSpeed, windGust, pressure, temp, humidity, solarRadiation);
            double hydroScore = calculateHydroScore(precip, precipProb);

            // Determine best energy source
            String bestSource = "Solar";
            double bestScore = solarScore;

            if (windScore > bestScore) {
                bestSource = "Wind";
                bestScore = windScore;
            }
            if (hydroScore > bestScore) {
                bestSource = "Hydro";
                bestScore = hydroScore;
            }

            // Display results
            displayText.append("🌍 RENEWABLE ENERGY ANALYSIS\n");
            displayText.append("Location: ").append(weatherData.optString("resolvedAddress", "N/A")).append("\n\n");

            displayText.append("⚡ OPTIMAL ENERGY SOURCE: ").append(bestSource.toUpperCase()).append("\n");
            displayText.append("Confidence Score: ").append(String.format("%.1f%%", bestScore * 100)).append("\n\n");

            displayText.append("📊 ENERGY SOURCE SCORES:\n");
            displayText.append("☀️ Solar Energy: ").append(String.format("%.1f%%", solarScore * 100)).append("\n");
            displayText.append("💨 Wind Energy: ").append(String.format("%.1f%%", windScore * 100)).append("\n");
            displayText.append("💧 Hydro Energy: ").append(String.format("%.1f%%", hydroScore * 100)).append("\n\n");

            displayText.append("🔍 ANALYSIS FACTORS:\n\n");

            displayText.append("☀️ SOLAR FACTORS:\n");
            displayText.append("• Solar Radiation: ").append(String.format("%.1f W/m²", solarRadiation)).append("\n");
            displayText.append("• UV Index: ").append(String.format("%.1f", uvIndex)).append("\n");
            displayText.append("• Cloud Cover: ").append(String.format("%.1f%%", cloudCover)).append("\n");
            displayText.append("• Day Length: ").append(String.format("%.1f hours", dayLengthMinutes / 60)).append("\n\n");

            displayText.append("💨 WIND FACTORS:\n");
            displayText.append("• Wind Speed: ").append(String.format("%.1f km/h", windSpeed)).append("\n");
            displayText.append("• Wind Gust: ").append(String.format("%.1f km/h", windGust)).append("\n");
            displayText.append("• Pressure: ").append(String.format("%.1f mb", pressure)).append("\n");
            displayText.append("• Temperature: ").append(String.format("%.1f°C", temp)).append("\n\n");

            displayText.append("💧 HYDRO FACTORS:\n");
            displayText.append("• Precipitation: ").append(String.format("%.1f mm", precip)).append("\n");
            displayText.append("• Precip. Probability: ").append(String.format("%.1f%%", precipProb)).append("\n\n");

            displayText.append("💡 RECOMMENDATIONS:\n");
            switch (bestSource) {
                case "Solar":
                    displayText.append("• Install solar panels facing south\n");
                    displayText.append("• Consider battery storage for cloudy days\n");
                    displayText.append("• Optimal tilt angle: ~").append(Math.round(weatherData.optDouble("latitude", 0))).append("°\n");
                    break;
                case "Wind":
                    displayText.append("• Install wind turbines in open areas\n");
                    displayText.append("• Consider vertical axis turbines for variable winds\n");
                    displayText.append("• Height matters - install as high as possible\n");
                    break;
                case "Hydro":
                    displayText.append("• Consider micro-hydro systems\n");
                    displayText.append("• Rainwater harvesting systems\n");
                    displayText.append("• Small-scale hydroelectric generators\n");
                    break;
            }

            textViewResult.setText(displayText.toString());

        } catch (JSONException e) {
            Log.e("WeatherApp", "Error parsing JSON for energy calculation", e);
            textViewResult.setText("Error calculating energy source: " + e.getMessage());
        }
    }

    private double calculateDayLength(String sunrise, String sunset) {
        try {
            String[] sunriseTime = sunrise.split(":");
            String[] sunsetTime = sunset.split(":");

            int sunriseMinutes = Integer.parseInt(sunriseTime[0]) * 60 + Integer.parseInt(sunriseTime[1]);
            int sunsetMinutes = Integer.parseInt(sunsetTime[0]) * 60 + Integer.parseInt(sunsetTime[1]);

            return Math.max(sunsetMinutes - sunriseMinutes, 720); // Default 12 hours if calculation fails
        } catch (Exception e) {
            return 720; // Default 12 hours
        }
    }

    private double normalize(double value, double min, double max) {
        if (max == min) return 0.5; // Avoid division by zero
        return Math.max(0, Math.min(1, (value - min) / (max - min)));
    }

    private double calculateSolarScore(double solarRadiation, double uvIndex, double cloudCover, double dayLengthMinutes) {
        // Normalize values based on typical ranges
        double solarNorm = normalize(solarRadiation, 0, 1000); // W/m²
        double uvNorm = normalize(uvIndex, 0, 12);
        double cloudNorm = normalize(cloudCover, 0, 100);
        double dayLengthNorm = normalize(dayLengthMinutes, 480, 960); // 8-16 hours

        return 0.4 * solarNorm + 0.3 * uvNorm + 0.2 * (1 - cloudNorm) + 0.1 * dayLengthNorm;
    }

    private double calculateWindScore(double windSpeed, double windGust, double pressure, double temp, double humidity, double solarRadiation) {
        // Normalize values based on typical ranges
        double windSpeedNorm = normalize(windSpeed, 0, 60); // km/h
        double windGustNorm = normalize(windGust, 0, 100); // km/h
        double pressureNorm = normalize(1013.25 - pressure, -50, 50); // Inverse pressure
        double tempNorm = normalize(temp, -20, 50); // °C
        double humidityNorm = normalize(humidity, 0, 100); // %
        double solarNorm = normalize(solarRadiation, 0, 1000); // W/m²

        return 0.4 * windSpeedNorm + 0.2 * windGustNorm + 0.15 * pressureNorm +
                0.15 * tempNorm + 0.05 * humidityNorm + 0.05 * solarNorm;
    }

    private double calculateHydroScore(double precip, double precipProb) {
        // Normalize values based on typical ranges
        double precipNorm = normalize(precip, 0, 50); // mm
        double precipProbNorm = normalize(precipProb, 0, 100); // %

        return 0.6 * precipNorm + 0.4 * precipProbNorm;
    }
}