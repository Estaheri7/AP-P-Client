package org.example.appclient.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.example.appclient.util.JwtManager;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AllEducationsController {
    private final Gson gson = new Gson();

    private static String profileEmail;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox contentBox;

    public void initialize() {
        ArrayList<HashMap<String, String>> educationList = fetchEducations();

        int i = 1;

        for (HashMap<String, String> education : educationList) {
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(10, 10, 10, 10));

            Label educationLabel = new Label("Education " + i);
            i++;
            educationLabel.setFont(new Font(18));
            educationLabel.setPadding(new Insets(0, 0, -10, 7));
            contentBox.getChildren().add(educationLabel);

            Label schoolNameLabel = new Label("School Name:");
            TextField schoolNameField = new TextField(education.get("schoolName"));
            schoolNameField.setEditable(false);
            schoolNameField.setDisable(true);
            grid.add(schoolNameLabel, 0, 0);
            grid.add(schoolNameField, 1, 0);

            Label fieldLabel = new Label("Field:");
            TextField fieldField = new TextField(education.get("field"));
            fieldField.setEditable(false);
            fieldField.setDisable(true);
            grid.add(fieldLabel, 0, 1);
            grid.add(fieldField, 1, 1);

            Label gradeLabel = new Label("Grade:");
            TextField gradeField = new TextField(education.get("grade"));
            gradeField.setPrefWidth(350);
            gradeField.setEditable(false);
            gradeField.setDisable(true);
            grid.add(gradeLabel, 0, 2);
            grid.add(gradeField, 1, 2);

            Label startDateLabel = new Label("Start Date:");
            TextField startDateField = new TextField(education.get("startDate"));
            startDateField.setEditable(false);
            startDateField.setDisable(true);
            grid.add(startDateLabel, 0, 3);
            grid.add(startDateField, 1, 3);

            Label endDateLabel = new Label("End Date:");
            TextField endDateField = new TextField(education.get("endDate"));
            endDateField.setEditable(false);
            endDateField.setDisable(true);
            grid.add(endDateLabel, 0, 4);
            grid.add(endDateField, 1, 4);

            Label communityLabel = new Label("Community:");
            TextField communityField = new TextField(education.get("community"));
            communityField.setEditable(false);
            communityField.setDisable(true);
            grid.add(communityLabel, 0, 5);
            grid.add(communityField, 1, 5);

            Label descriptionLabel = new Label("Description:");
            TextField descriptionField = new TextField(education.get("description"));
            descriptionField.setEditable(false);
            descriptionField.setDisable(true);
            grid.add(descriptionLabel, 0, 6);
            grid.add(descriptionField, 1, 6);

            contentBox.getChildren().add(grid);
        }
    }

    private ArrayList<HashMap<String, String>> fetchEducations() {
        ArrayList<HashMap<String, String>> educations = new ArrayList<>();
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());

                URL url = new URL("http://localhost:8080/user/educations/" + profileEmail);
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

                        educations = gson.fromJson(response.toString(), new TypeToken<List<HashMap<String, String>>>(){}.getType());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return educations;
    }

    public static void setProfileEmail(String profileEmail) {
        AllEducationsController.profileEmail = profileEmail;
    }
}
