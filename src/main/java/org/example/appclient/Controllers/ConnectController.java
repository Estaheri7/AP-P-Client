package org.example.appclient.Controllers;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.appclient.util.JwtManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class ConnectController {

    private final Gson gson = new Gson();

    @FXML
    private TextField noteTextField;

    @FXML
    private Button sendButton;

    @FXML
    private Label pendingErrorLabel;

    @FXML
    private Label successLabel;

    @FXML
    void onSendButton(ActionEvent event) {
        sendConnectionRequest(ProfileController.getProfileEmail());
    }

    private void sendConnectionRequest(String receiver) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                pendingErrorLabel.setVisible(false);
                successLabel.setVisible(false);
                String senderEmail = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());

                URL url = new URL("http://localhost:8080/send-connect/" + receiver);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String note = noteTextField.getText();
                HashMap<String, String> connectData = new HashMap<>();
                connectData.put("sender", senderEmail);
                connectData.put("receiver", receiver);
                connectData.put("note", note);

                String jsonData = gson.toJson(connectData);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonData.getBytes());
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    successLabel.setVisible(true);
                } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                    pendingErrorLabel.setVisible(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isConnectionPending(String receiver) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {

                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());

                URL url = new URL("http://localhost:8080/connections/pending/" + email + "?profile=" + receiver);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                int responseCode = connection.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isConnected(String receiver) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/connect/isAccepted/" + receiver);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
