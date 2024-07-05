package org.example.appclient.Controllers;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.appclient.util.JwtManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class ShareController {

    @FXML
    private ScrollPane mainScrollPane;

    private VBox usersVBox;

    @FXML
    private Label shareLabel;

    private static Label tempLabel;

    private static HashMap<String, String> post;

    public void initialize() {
        usersVBox = new VBox();
        usersVBox.setSpacing(10);
        mainScrollPane.setContent(usersVBox);
        fillDialog();
    }

    private void fillDialog() {
        ArrayList<String> senderEmails = FetcherEmail.fetchEmails("connections", "sender", ProfileController.getProfileEmail());
        usersVBox.getChildren().clear();
        for (String senderEmail : senderEmails) {
            HashMap<String, String> userDetail = FetcherEmail.fetchProfileDetailsByEmail(senderEmail);
            addUserToDialog(userDetail);
        }
    }

    private void addUserToDialog(HashMap<String, String> userDetail) {
        String name = userDetail.get("firstName") + " " + userDetail.get("lastName");
        String email = userDetail.get("email");
        String avatarUrl = userDetail.get("avatar_url");

        HBox userHBox = new HBox();
        userHBox.setAlignment(Pos.CENTER_LEFT);
        userHBox.setSpacing(10);
        Label nameLabel = new Label(name);
        ImageView avatar = PostController.handleAvatar(email, avatarUrl, tempLabel);
        Circle circle = new Circle(25,25,25);
        avatar.setClip(circle);
        Button button = new Button("Share");
        button.getStylesheets().addAll(getClass().getResource("/org/example/appclient/css/Button.css").toExternalForm());
        button.setOnAction(e -> sharePost(Integer.parseInt(post.get("id")), email));
        userHBox.getChildren().addAll(avatar, nameLabel, button);
        usersVBox.getChildren().add(userHBox);
    }

    private void sharePost(int postId, String email) {
        String postLink = "http://localhost:8080/post/" + postId;
        HashMap<String, String> chatData = new HashMap<>();
        chatData.put("message", postLink);
        chatData.put("receiver", email);

        Gson gson = new Gson();
        String jsonData = gson.toJson(chatData);

        new Thread(() -> sendMessage(jsonData, email)).start();
    }

    private void sendMessage(String jsonData, String receiver) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/send-message/" + receiver);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(jsonData.getBytes());
                    outputStream.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Platform.runLater(() -> {
                        Stage stage = (Stage) shareLabel.getScene().getWindow();
                        stage.close();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setPost(HashMap<String, String> post) {
        ShareController.post = post;
    }

    public static void setTempLabel(Label tempLabel) {
        ShareController.tempLabel = tempLabel;
    }
}
