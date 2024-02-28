package com.example.pogoda;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class pogoda extends Application {

    private static final String API_KEY = "ff90414bbf3ac820348bcc77183616bb";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";

    private TextField cityTextField;
    private TextArea resultTextArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Weather API");

        // ----- nagłówek -----
        Label headerLabel = new Label("PROGNOZA POGODY");
        headerLabel.setStyle("-fx-font-size: 30; -fx-font-weight: bold; -fx-font-family: serif;");

        // ----- pole miasta -----
        cityTextField = new TextField();
        cityTextField.setStyle("-fx-background-color: lightcoral;");
        cityTextField.setPromptText("Wprowadź nazwę miasta:");

        // ----- przycisk wyszukiwania -----
        Button searchButton = new Button("Szukaj");
        searchButton.setStyle("-fx-background-color: indianred;");
        searchButton.setOnAction(e -> searchWeather());

        // ----- pole wyników -----
        resultTextArea = new TextArea();
        resultTextArea.setEditable(false);

        // ----- przycisk zamykania -----
        Button closeButton = new Button("Zamknij");
        closeButton.setStyle("-fx-background-color: indianred;");
        closeButton.setOnAction(e -> primaryStage.close());

        // ----- layout -----
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(headerLabel, cityTextField, searchButton, resultTextArea, closeButton);

        // ----- scena -----
        Scene scene = new Scene(layout, 650, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void searchWeather() {
        String cityName = cityTextField.getText();
        if (!cityName.isEmpty()) {
            String apiUrl = String.format("%s?q=%s&appid=%s&lang=pl&units=metric", API_URL, cityName, API_KEY);

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream responseStream = connection.getInputStream();
                Scanner scanner = new Scanner(responseStream).useDelimiter("\\A");
                String responseBody = scanner.hasNext() ? scanner.next() : "";

                JSONObject json = new JSONObject(responseBody);
                displayWeatherInfo(json);

            } catch (IOException e) {
                resultTextArea.setText("Brak wyniku.");
            }
        } else {
            resultTextArea.setText("Wprowadź nazwę miasta");
        }
    }

    private void displayWeatherInfo(JSONObject json) {
        String description = json.getJSONArray("weather").getJSONObject(0).getString("description");
        JSONObject mainSection = json.getJSONObject("main");

        // Pobierz wartość "speed" jako liczba
        double windSpeed = json.getJSONObject("wind").getDouble("speed");
        String windSpeedString = String.format("%.2f", windSpeed);

        String windDirection = getWindDirection(json.getJSONObject("wind").optDouble("deg"));
        String rainInfo = getRainSnowInfo(json, "rain");
        String snowInfo = getRainSnowInfo(json, "snow");

        // Pobierz wartość "all" jako liczba całkowita
        int clouds = json.getJSONObject("clouds").getInt("all");
        String cityName = json.getString("name");

        String result = String.format("Opis: %s\nTemperatura: %.2f°C\nWilgotność: %d%%\nCiśnienie: %.2f hPa\nOdczuwalna: %.2f°C\nMin. temperatura: %.2f°C\nMax. temperatura: %.2f°C\nPrędkość wiatru: %s m/s\nKierunek wiatru: %s\nDeszcz: %s\nŚnieg: %s\nZachmurzenie: %d%%\nMiasto: %s",
                description, mainSection.getDouble("temp"), mainSection.getInt("humidity"),
                mainSection.getDouble("pressure"), mainSection.getDouble("feels_like"),
                mainSection.getDouble("temp_min"), mainSection.getDouble("temp_max"),
                windSpeedString, windDirection, rainInfo, snowInfo, clouds, cityName);

        resultTextArea.setText(result);
    }

    private String getWindDirection(double degree) {
        if (degree >= 337.5 || degree < 22.5) {
            return "N";
        } else if (degree >= 22.5 && degree < 67.5) {
            return "NE";
        } else if (degree >= 67.5 && degree < 112.5) {
            return "E";
        } else if (degree >= 112.5 && degree < 157.5) {
            return "SE";
        } else if (degree >= 157.5 && degree < 202.5) {
            return "S";
        } else if (degree >= 202.5 && degree < 247.5) {
            return "SW";
        } else if (degree >= 247.5 && degree < 292.5) {
            return "W";
        } else {
            return "NW";
        }
    }

    private String getRainSnowInfo(JSONObject json, String key) {
        if (json.optJSONObject(key) != null) {
            return json.getJSONObject(key).toString();
        } else {
            return "N/A";
        }
    }
}