package org.example.appclient.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.appclient.DataValidator.EducationValidator;
import org.example.appclient.util.JwtManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class UpdateEducationController {
    private final Gson gson = new Gson();

    @FXML
    private Button applyButton;

    @FXML
    private TextField communityTextField;

    @FXML
    private TextField descriptionTextField;

    @FXML
    private Label endDateError;

    @FXML
    private TextField endDateTextField;

    @FXML
    private Label fieldError;

    @FXML
    private TextField fieldTextField;

    @FXML
    private Label gradeError;

    @FXML
    private TextField gradeTextField;

    @FXML
    private Label schoolNameError;

    @FXML
    private TextField schoolTextField;

    @FXML
    private Label startDateError;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    void onApplyButton(ActionEvent event) {
        new Thread(() -> apply()).start();
    }

    private void apply() {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            schoolNameError.setVisible(false);
            endDateError.setVisible(false);
            fieldError.setVisible(false);
            gradeError.setVisible(false);
            startDateError.setVisible(false);
            try {
                String schoolName = schoolTextField.getText();
                String grade = gradeTextField.getText();
                String endDate = endDatePicker.getValue() != null ? endDatePicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
                String startDate = startDatePicker.getValue() != null ? startDatePicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
                String field = fieldTextField.getText();
                String community = communityTextField.getText();
                String description = descriptionTextField.getText();

                if (!checkValidation()) {
                    return;
                }

                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());

                URL url = new URL("http://localhost:8080/profile/update/education/" + email);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                HashMap<String, String> educationData = new HashMap<>();
                educationData.put("schoolName", schoolName);
                educationData.put("grade", grade);
                educationData.put("endDate", endDate);
                educationData.put("startDate", startDate);
                educationData.put("field", field);
                educationData.put("community", community);
                educationData.put("description", description);

                String jsonData = gson.toJson(educationData);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonData.getBytes());
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    ProfileController.reloadProfile(applyButton);
                    Platform.runLater(() -> {
                        Stage stage = (Stage) applyButton.getScene().getWindow();
                        stage.close();
                    });
                } else {
                    System.out.println(connection.getResponseMessage());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private HashMap<String, String> fillUpdatePage() {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());

                URL url = new URL("http://localhost:8080/user/education/" + email);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                int responseCode = connection.getResponseCode();
                HashMap<String, String> educationData = new HashMap<>();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = br.readLine()) != null) {
                            response.append(inputLine);
                        }

                        Type type = new TypeToken<HashMap<String, String>>(){}.getType();
                        educationData = gson.fromJson(response.toString(), type);
                    }
                }

                return educationData;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private boolean checkValidation() {
        String schoolName = schoolTextField.getText();
        String grade = gradeTextField.getText();
        String endDate = endDatePicker.getValue() != null ? endDatePicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
        String startDate = startDatePicker.getValue() != null ? startDatePicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
        String field = fieldTextField.getText();

        if (!EducationValidator.schoolNameValidator(schoolName)) {
            schoolNameError.setVisible(true);
            return false;
        }
        if (!EducationValidator.gradeValidator(grade)) {
            gradeError.setVisible(true);
            return false;
        }
        if (!EducationValidator.dateValidator(startDate)) {
            startDateError.setVisible(true);
            return false;
        }
        if (!EducationValidator.dateValidator(endDate)) {
            endDateError.setVisible(true);
            return false;
        }
        if (!EducationValidator.fieldValidator(field)) {
            fieldError.setVisible(true);
            return false;
        }
        return true;
    }

    public void initialize() {
        new Thread(() -> {
            Platform.runLater(() -> {
                HashMap<String, String> educationData = fillUpdatePage();

                schoolTextField.setText(educationData.get("schoolName"));
                gradeTextField.setText(educationData.get("grade"));
                fieldTextField.setText(educationData.get("field"));
                communityTextField.setText(educationData.get("community"));
                descriptionTextField.setText(educationData.get("description"));
            });
        }).start();
    }
}
