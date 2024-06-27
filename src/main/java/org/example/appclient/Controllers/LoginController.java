package org.example.appclient.Controllers;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Effect;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.appclient.util.JwtManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class LoginController {
    private final Gson gson = new Gson();

    @FXML
    private Button signInButton;

    @FXML
    private TextField emailTextField;

    @FXML
    private Button loginButton;

    @FXML
    private Label loggingErrorLabel;

    @FXML
    private Label successLabel;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private TextField passwordTextFieldVisible;

    @FXML
    private CheckBox passwordCheckBox;

    @FXML
    void onCreateAccountButton(ActionEvent event) {
        try {
            Parent signupPage = FXMLLoader.load(getClass().getResource("/org/example/appclient/signup.fxml"));

            Scene signupPageScene = new Scene(signupPage);
            Stage currentStage = (Stage) signInButton.getScene().getWindow();
            currentStage.setScene(signupPageScene);
            currentStage.setFullScreen(true);
            currentStage.setFullScreenExitHint("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onSignInButton(ActionEvent event) {
        HttpURLConnection connection = null;
        loggingErrorLabel.setVisible(false);
        successLabel.setVisible(false);
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
                    HashMap<String, String> jwt = gson.fromJson(token, HashMap.class);
                    JwtManager.setJwtToken(jwt.get("token"));
                    ProfileController.setProfileEmail((String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken()));
                    successLabel.setVisible(true);
                    try {
                        openProfile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                loggingErrorLabel.setVisible(true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void showCheckbox(ActionEvent event) {
        if (passwordCheckBox.isSelected()) {
            passwordTextFieldVisible.setVisible(true);
            passwordTextFieldVisible.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
        } else {
            passwordTextFieldVisible.setVisible(false);
            passwordTextFieldVisible.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
        }
    }

    private void openProfile() throws IOException {
        Parent profilePage = FXMLLoader.load(getClass().getResource("/org/example/appclient/profile.fxml"));
        Scene profilePageScene = new Scene(profilePage);
        Stage currentStage = (Stage) signInButton.getScene().getWindow();
        currentStage.setScene(profilePageScene);
        currentStage.setFullScreen(true);
        currentStage.setFullScreenExitHint("");
    }

    public void initialize() {
        passwordTextFieldVisible.textProperty().bindBidirectional(passwordTextField.textProperty());
    }
}
