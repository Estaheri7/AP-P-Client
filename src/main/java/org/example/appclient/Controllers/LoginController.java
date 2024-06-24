package org.example.appclient.Controllers;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.appclient.util.JwtManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class LoginController {
    private final Gson gson = new Gson();

    @FXML
    private Button createAccountButton;

    @FXML
    private TextField emailTextField;

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private CheckBox visibleCheckBox;

    @FXML
    void onCreateAccountButton(ActionEvent event) {

    }

    @FXML
    void onLoginButton(ActionEvent event) {
        HttpURLConnection connection = null;
        try {
            String email = emailTextField.getText();
            String password = passwordTextField.getText();

            HashMap<String, String> userPair = new HashMap<>();
            userPair.put("email", email);
            userPair.put("password", password);
            String jsonData = gson.toJson(userPair);
            URL url = new URL("http://localhost:8080/login");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonData.getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String token = reader.readLine();
                    JwtManager.setJwtToken(token);
                    System.out.println(JwtManager.getJwtToken());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onVisible(ActionEvent event) {

    }
}
