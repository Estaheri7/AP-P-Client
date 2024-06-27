package org.example.appclient.Controllers;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.appclient.DataValidator.EducationValidator;
import org.example.appclient.util.JwtManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class AddEducationController {

    private final Gson gson = new Gson();

    @FXML
    private Button addButton;

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
    private TextField startDateField;

    @FXML
    void onAddButton(ActionEvent event) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());

                String schoolName = schoolTextField.getText();
                String field = fieldTextField.getText();
                String grade = gradeTextField.getText();
                String endDate = endDateTextField.getText();
                String startDate = startDateField.getText();
                String description = descriptionTextField.getText();
                String community = communityTextField.getText();

                if (!checkValidation()) {
                    return;
                }

                URL url = new URL("http://localhost:8080/profile/add/education/" + email);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                HashMap<String, String> educationData = new HashMap<>();
                educationData.put("schoolName", schoolName);
                educationData.put("field", field);
                educationData.put("grade", grade);
                educationData.put("endDate", endDate);
                educationData.put("startDate", startDate);
                educationData.put("description", description);
                educationData.put("community", community);

                String jsonData = gson.toJson(educationData);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonData.getBytes());
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    ProfileController.reloadProfile(addButton);
                    Stage stage = (Stage) addButton.getScene().getWindow();
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

    private boolean checkValidation() {
        String schoolName = schoolTextField.getText();
        String grade = gradeTextField.getText();
        String endDate = endDateTextField.getText();
        String startDate = startDateField.getText();
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
}

