package org.example.appclient.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.appclient.util.JwtManager;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class AddPostController {

    private static String postId;

    private static final Gson gson = new Gson();

    @FXML
    private Button addMedia;

    @FXML
    private TextArea contentTextArea;

    @FXML
    private TextField titleTextField;

    @FXML
    private Button doneButton;

    @FXML
    private Label mediaAddedSuccessfully;

    private File mediaFile;

    @FXML
    void onAddMediaButton(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.pdf");
        FileChooser.ExtensionFilter audioFilter = new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac", "*.ogg");
        FileChooser.ExtensionFilter videoFilter = new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mkv", "*.avi");
        fileChooser.getExtensionFilters().addAll(imageFilter, audioFilter, videoFilter);
        mediaFile = fileChooser.showOpenDialog(doneButton.getScene().getWindow());
        if (mediaFile != null) {
            mediaAddedSuccessfully.setVisible(true);
        }
    }

    @FXML
    void onDoneButton(ActionEvent event) {
        new Thread(() -> {
            sendPostContent();
            if (mediaFile != null) {
                addMedia();
            }
        }).start();
    }

    private void sendPostContent() {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());

                URL url = new URL("http://localhost:8080/posts/add/" + email);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                HashMap<String, String> content = new HashMap<>();
                String title = titleTextField.getText();
                String textAreaContent = contentTextArea.getText();
                content.put("title", title);
                content.put("content", textAreaContent);

                String jsonData = gson.toJson(content);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonData.getBytes());
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                System.out.println(responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Platform.runLater(() -> {
                        Stage stage = (Stage) doneButton.getScene().getWindow();
                        stage.close();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addMedia() {
        try {
            String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());
            getLastPost(email);
            MediaController.uploadFile(mediaFile, "/posts/add-media", email + "?id=" + postId);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getLastPost(String email) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/lastPost/" + email);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    HashMap<String, String> post;
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }

                        Type type = new TypeToken<HashMap<String, String>>(){}.getType();
                        post = gson.fromJson(response.toString(), type);
                        int id = Integer.parseInt(post.get("id"));
                        postId = String.valueOf(id);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

