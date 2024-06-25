package org.example.appclient.Controllers;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.appclient.DataValidator.UserValidator;

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
    private TextField emailTextField;

    @FXML
    private TextField lastNameTextField;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private TextField rPasswordTextField;

    @FXML
    private Label nameError;

    @FXML
    private Label emailError;

    @FXML
    private Label lastNameError;

    @FXML
    private Label passwordError;

    @FXML
    private Label rPasswordError;

    @FXML
    private Label signupError;

    @FXML
    private Label signupSuccess;

    @FXML
    private Button signupButton;

    @FXML
    private Button signInButton;

    @FXML
    void onSignupButton(ActionEvent event) {
        HttpURLConnection connection = null;
        nameError.setVisible(false);
        lastNameError.setVisible(false);
        emailError.setVisible(false);
        passwordError.setVisible(false);
        rPasswordError.setVisible(false);
        signupError.setVisible(false);
        signupSuccess.setVisible(false);
        try {
            String email = emailTextField.getText();
            String name = nameTextField.getText();
            String lastName = lastNameTextField.getText();
            String password = passwordTextField.getText();
            String rPassword = rPasswordTextField.getText();

            if (!UserValidator.nameValidator(name)) {
                nameError.setText("Invalid format");
                nameError.setVisible(true);
                return;
            } else if (!UserValidator.nameValidator(lastName)) {
                lastNameError.setText("Invalid format");
                lastNameError.setVisible(true);
                return;
            } else if (!UserValidator.emailValidator(email)) {
                emailError.setText("Invalid format");
                emailError.setVisible(true);
                return;
            } else if (!UserValidator.passwordValidator(password)) {
                passwordError.setText("Invalid format");
                passwordError.setVisible(true);
                return;
            }

            if (!password.equals(rPassword)) {
                rPasswordError.setText("Not same");
                rPasswordError.setVisible(true);
                return;
            }

            HashMap<String, String> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("firstName", name);
            userData.put("lastName", lastName);
            userData.put("password", password);

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
        currentStage.setScene(loginScene);
        currentStage.setFullScreen(true);
        currentStage.setFullScreenExitHint("");
    }
}

