package org.example.appclient.Controllers;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.appclient.DataValidator.UserValidator;
import org.example.appclient.util.JwtManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class EditIntroController {

    private final Gson gson = new Gson();

    @FXML
    private DialogPane dialogPane;

    @FXML
    private TextField additionalNameTField;

    @FXML
    private Label cityError;

    @FXML
    private TextField cityTField;

    @FXML
    private Label countryError;

    @FXML
    private TextField countryTField;

    @FXML
    private TextField firstNameTField;

    @FXML
    private Label headlineError;

    @FXML
    private TextArea headlineTField;

    @FXML
    private Label lastNameError;

    @FXML
    private TextField lastNameTField;

    @FXML
    private Label nameError;

    @FXML
    private Button applyButton;

    @FXML
    private Button cancelButton;

    @FXML
    private void onApplyButton(ActionEvent event) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            nameError.setVisible(false);
            lastNameError.setVisible(false);
            headlineError.setVisible(false);
            cityError.setVisible(false);
            countryError.setVisible(false);
            try {
                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());

                String firstName = firstNameTField.getText();
                String lastName = lastNameTField.getText();
                String AdditionalName = additionalNameTField.getText();
                String headline = headlineTField.getText();
                String city = cityTField.getText();
                String country = countryTField.getText();

                if (!checkDataValidate()) {
                    return;
                }

                URL url = new URL("http://localhost:8080/profile/update/" + email);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                HashMap<String, String> userData = new HashMap<>();
                userData.put("firstName", firstName);
                userData.put("lastName", lastName);
                userData.put("additionalName", AdditionalName);
                userData.put("headline", headline);
                userData.put("country", country);
                userData.put("city", city);

                String jsonData = gson.toJson(userData);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonData.getBytes());
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    ProfileController.reloadProfile(applyButton);
                    Stage stage = (Stage) applyButton.getScene().getWindow();
                    stage.close();
                } else {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                        System.out.println(br.readLine());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkDataValidate() {
        boolean flag = true;
        String firstName = firstNameTField.getText();
        String lastName = lastNameTField.getText();
        String headline = headlineTField.getText();
        String city = cityTField.getText();
        String country = countryTField.getText();

        if (!UserValidator.nameValidator(firstName)) {
            nameError.setVisible(true);
            flag = false;
        }
        if (!UserValidator.nameValidator(lastName)) {
            lastNameError.setVisible(true);
            flag = false;
        }
        if (headline == null || headline.isEmpty()) {
            headlineError.setVisible(true);
            flag = false;
        }
        if (city == null || city.isEmpty()) {
            cityError.setVisible(true);
            flag = false;
        }
        if (country == null || country.isEmpty()) {
            countryError.setVisible(true);
            flag = false;
        }

        return flag;
    }

    @FXML
    private void onCancelButton(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void initialize() {
    }
}

