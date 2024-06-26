package org.example.appclient.Controllers;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.example.appclient.util.JwtManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class UpdateSkillController {

    private final Gson gson = new Gson();

    private static String profileEmail;

    @FXML
    private Button saveButton;

    @FXML
    private TextField skill1TextField;

    @FXML
    private TextField skill2TextField;

    @FXML
    private TextField skill3TextField;

    @FXML
    private TextField skill4TextField;

    @FXML
    private TextField skill5TextField;

    @FXML
    void onSaveButton(ActionEvent event) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                HashMap<String, String> skillData = getStringStringHashMap();

                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());
                URL url = new URL("http://localhost:8080/profile/update/skill/" + email);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("AUTHORIZATION", "Bearer " + JwtManager.getJwtToken());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String jsonData = gson.toJson(skillData);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonData.getBytes());
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (!(responseCode == HttpURLConnection.HTTP_OK)) {
                    System.out.println(connection.getResponseMessage());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private HashMap<String, String> getStringStringHashMap() {
        String skill1 = skill1TextField.getText();
        String skill2 = skill2TextField.getText();
        String skill3 = skill3TextField.getText();
        String skill4 = skill4TextField.getText();
        String skill5 = skill5TextField.getText();

        HashMap<String, String> skillData = new HashMap<>();
        skillData.put("skill1", skill1);
        skillData.put("skill2", skill2);
        skillData.put("skill3", skill3);
        skillData.put("skill4", skill4);
        skillData.put("skill5", skill5);
        return skillData;
    }

    public void initialize() {
        HashMap<String, String> skills = fetchSkills();

        skill1TextField.setText(skills.get("skill1"));
        skill2TextField.setText(skills.get("skill2"));
        skill3TextField.setText(skills.get("skill3"));
        skill4TextField.setText(skills.get("skill4"));
        skill5TextField.setText(skills.get("skill5"));
    }

    private HashMap<String, String> fetchSkills() {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/user/skill/" + profileEmail);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                HashMap<String, String> skills = new HashMap<>();
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        skills = gson.fromJson(response.toString(), HashMap.class);
                    }
                }
                return skills;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void setProfileEmail(String profileEmail) {
        UpdateSkillController.profileEmail = profileEmail;
    }

}
