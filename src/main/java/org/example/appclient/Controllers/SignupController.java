package org.example.appclient.Controllers;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.appclient.DataValidator.UserValidator;
import org.example.appclient.LinkedInApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class SignupController {
    private final Gson gson = new Gson();

    @FXML
    private Label cityErrorLabel;

    @FXML
    private TextField cityTextField;

    @FXML
    private Label countryErrorLabel;

    @FXML
    private TextField countryTextField;

    @FXML
    private Label emailErrorLabel;

    @FXML
    private TextField emailTextField;

    @FXML
    private Label headlineErrorLabel;

    @FXML
    private TextArea headlineTextArea;

    @FXML
    private Label lastNameErrorLabel;

    @FXML
    private TextField lastNameTextField;

    @FXML
    private Label nameErrorLabel;

    @FXML
    private TextField nameTextField;

    @FXML
    private Label passwordErrorLabel;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Label rPassErrorLabel;

    @FXML
    private TextField rPassTextField;

    @FXML
    private VBox rootVBox;

    @FXML
    private Button signInbutton;

    @FXML
    private Button signupButton;

    @FXML
    private Label signupError;

    @FXML
    private Label signupSuccess;

    @FXML
    void onSignupButton(ActionEvent event) {
        HttpURLConnection connection = null;
        nameErrorLabel.setVisible(false);
        lastNameErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
        rPassErrorLabel.setVisible(false);
        countryErrorLabel.setVisible(false);
        cityErrorLabel.setVisible(false);
        headlineErrorLabel.setVisible(false);
        signupError.setVisible(false);
        signupSuccess.setVisible(false);
        try {
            String email = emailTextField.getText();
            String name = nameTextField.getText();
            String lastName = lastNameTextField.getText();
            String password = passwordTextField.getText();
            String rPassword = rPassTextField.getText();
            String country = countryTextField.getText();
            String city = cityTextField.getText();
            String headline = headlineTextArea.getText();

            if (!UserValidator.nameValidator(name)) {
                nameErrorLabel.setText("Invalid format");
                nameErrorLabel.setVisible(true);
                return;
            } else if (!UserValidator.nameValidator(lastName)) {
                lastNameErrorLabel.setText("Invalid format");
                lastNameErrorLabel.setVisible(true);
                return;
            } else if (!UserValidator.emailValidator(email)) {
                emailErrorLabel.setText("Invalid format");
                emailErrorLabel.setVisible(true);
                return;
            } else if (!UserValidator.passwordValidator(password)) {
                passwordErrorLabel.setText("Invalid format");
                passwordErrorLabel.setVisible(true);
                return;
            }

            if (!password.equals(rPassword)) {
                rPassErrorLabel.setText("Not same");
                rPassTextField.setVisible(true);
                return;
            }
            if(country==null || country.isEmpty()) {
                countryErrorLabel.setText("Country cannot be empty");
            }
            else if(city==null || city.isEmpty()) {
                cityErrorLabel.setText("City cannot be empty");
            }
            else if(headline==null || headline.isEmpty()) {
                headlineErrorLabel.setText("Headline cannot be empty");
            }

            HashMap<String, String> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("firstName", name);
            userData.put("lastName", lastName);
            userData.put("password", password);
            userData.put("country", country);
            userData.put("city", city);
            userData.put("headline", headline);

            URL url = new URL("http://localhost:8080/signup");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonData = gson.toJson(userData);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonData.getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                signupSuccess.setVisible(true);
                    try {
                        loadLoginPage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                signupError.setVisible(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onSignInButton() {
        try {
            loadLoginPage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLoginPage() throws IOException {
        Parent loginPage = FXMLLoader.load(getClass().getResource("/org/example/appclient/login.fxml"));

        Scene loginScene = new Scene(loginPage);
        Stage currentStage = (Stage) signupButton.getScene().getWindow();

        Stage loginStage = new Stage();
        loginStage.setScene(loginScene);
        LinkedInApp.setIcon(loginStage);
        loginStage.initOwner(currentStage);
        loginStage.initModality(Modality.APPLICATION_MODAL);

        loginStage.setFullScreen(true);
        loginStage.setFullScreenExitHint("");

        currentStage.hide();
        loginStage.show();
    }

    public void initialize() {
        headlineTextArea.setStyle("-fx-background-color: #403636;");
    }
}

