package org.example.appclient.Controllers;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.appclient.DataValidator.ContactValidator;
import org.example.appclient.util.JwtManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class ContactController {

    private final Gson gson = new Gson();

    @FXML
    private Label addressErrorLabel;

    @FXML
    private TextField addressTField;

    @FXML
    private Button applyButton;

    @FXML
    private Label bdErrorLabel;

    @FXML
    private TextField birthDateTField;

    @FXML
    private TextField fastConnectTField;

    @FXML
    private Label phoneErrorLabel;

    @FXML
    private TextField phoneNumberTField;

    @FXML
    private TextField viewLinkTField;

    @FXML
    private Label viewlinkErrorLabel;

    @FXML
    void onApplyButton(ActionEvent event) {
        updateContact();
    }

    private void updateContact() {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            phoneErrorLabel.setVisible(false);
            bdErrorLabel.setVisible(false);
            viewlinkErrorLabel.setVisible(false);
            addressErrorLabel.setVisible(false);
            try {
                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());

                String phoneNumber = phoneNumberTField.getText();
                String address = addressTField.getText();
                String birthDate = birthDateTField.getText();
                String fastConnect = fastConnectTField.getText();
                String viewLink = viewLinkTField.getText();

                if (!dataValidator()) {
                    return;
                }

                URL url = new URL("http://localhost:8080/profile/update/contact/" + email);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                HashMap<String, String> newContact = new HashMap<>();
                newContact.put("phoneNumber", phoneNumber);
                newContact.put("address", address);
                newContact.put("birthDate", birthDate);
                newContact.put("fastConnect", fastConnect);
                newContact.put("viewLink", viewLink);

                String jsonData = gson.toJson(newContact);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonData.getBytes());
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Stage stage = (Stage) applyButton.getScene().getWindow();
                    stage.close();
                } else {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        System.out.println(br.readLine());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private HashMap<String, String> fetchContact() {
        HashMap<String, String> contact = new HashMap<>();
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());
                URL url = new URL("http://localhost:8080/user/contact/" + email);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = br.readLine()) != null) {
                            response.append(inputLine);
                        }

                        contact = gson.fromJson(response.toString(), HashMap.class);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return contact;
    }

    private void fillContactUpdate() {
        HashMap<String, String> contact = fetchContact();
        phoneNumberTField.setText(contact.get("phoneNumber"));
        addressTField.setText(contact.get("address"));
        birthDateTField.setText(contact.get("birthDate"));
        fastConnectTField.setText(contact.get("fastConnect"));
        viewLinkTField.setText(contact.get("viewLink"));
    }

    private boolean dataValidator() {
        String phoneNumber = phoneNumberTField.getText();
        String address = addressTField.getText();
        String birthDate = birthDateTField.getText();
        String viewLink = viewLinkTField.getText();

        if (!ContactValidator.phoneNumberValidator(phoneNumber)) {
            phoneErrorLabel.setVisible(true);
            return false;
        }
        if (!ContactValidator.viewLinkValidator(viewLink)) {
            viewlinkErrorLabel.setVisible(true);
            return false;
        }
        if (!ContactValidator.dateValidator(birthDate)) {
            bdErrorLabel.setVisible(true);
            return false;
        }
        if (address.isEmpty()) {
            addressErrorLabel.setVisible(true);
            return false;
        }
        return true;
    }

    public void initialize() {
        fillContactUpdate();
    }
}
