package org.example.appclient.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.geometry.Insets;
import javafx.stage.Window;
import org.example.appclient.util.JwtManager;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static org.example.appclient.Controllers.FetcherEmail.fetchProfileDetailsByEmail;
import static org.example.appclient.Controllers.FetcherEmail.goToProfile;

public class NotificationController {
    private final Gson gson = new Gson();

    @FXML
    private Label notificationLabel;

    @FXML
    private ScrollPane userScrollPane;

    private VBox notificationVBox;

    private final ArrayList<HashMap<String, String>> followingNotifications = new ArrayList<>();
    private final ArrayList<HashMap<String, String>> userProfiles = new ArrayList<>();

    public void initialize() {
        notificationVBox = new VBox();
        notificationVBox.setSpacing(10);
        notificationVBox.setPadding(new Insets(10, 10, 10, 10));
        userScrollPane.setContent(notificationVBox);
        notificationVBox.getChildren().clear();
        displayNotifications();
    }

    private void displayNotifications() {
        Thread thread = new Thread(() -> {
            searchNotifications();
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        int i = 0;
        while (i < followingNotifications.size()) {
            HashMap<String, String> notification = followingNotifications.get(i);
            HashMap<String, String> user = userProfiles.get(i);
            fillNotification(user, notification);
            i++;
        }
    }

    private void fillNotification(HashMap<String, String> user, HashMap<String, String> notification) {
        Platform.runLater(() -> {
            HBox notificationRow = new HBox();
            VBox detailVBox = new VBox();
            HBox userDetail = new HBox();
            HBox notificationDetail = new HBox();
            notificationRow.setSpacing(10);
            detailVBox.setSpacing(7);

            String avatarURL = user.get("avatar_url");
            String email = user.get("email");
            String title = notification.get("title");
            String message = notification.get("message");
            String timestamp = notification.get("timestamp");
            int postId = Integer.parseInt(notification.get("postId"));
            String name = user.get("firstName") + " " + user.get("lastName");

            ImageView imageView = new ImageView();
            Label nameLabel = new Label(name);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16");
            Label timestampLabel = new Label(timestamp);
            timestampLabel.setTranslateX(10);
            timestampLabel.setTranslateY(5);
            Label titleLabel = new Label(title);
            Label messageLabel = new Label(message);

            if (avatarURL != null && !avatarURL.isEmpty()) {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080/user/avatar/" + email).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        Image image = new Image(inputStream);
                        imageView = new ImageView(image);
                        imageView.setFitWidth(50);
                        imageView.setFitHeight(50);
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Image image = new Image(FetcherEmail.class.getResource("/org/example/appclient/images/defaultAvatar.png").toExternalForm());
                imageView = new ImageView(image);
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
            }

            imageView.setOnMouseClicked(event -> goToProfile(email, nameLabel));
            Circle circle = new Circle(25, 25, 25);
            imageView.setClip(circle);

            userDetail.getChildren().addAll(nameLabel, timestampLabel);
            notificationDetail.getChildren().addAll(titleLabel, messageLabel);

            Thread thread = new Thread(() -> {
                if (postId != 0) {
                    createPostLink(postId, notificationDetail);
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            detailVBox.getChildren().addAll(userDetail, notificationDetail);
            notificationRow.getChildren().addAll(imageView, detailVBox);
            notificationVBox.getChildren().add(notificationRow);
        });
    }

    private void createPostLink(int postId, HBox notificationDetail) {
        HashMap<String, String> post = fetchPost(postId);
        HashMap<String, String> user = fetchProfileDetailsByEmail(post.get("author"));
        String avatarURl = user.get("avatar_url");
        String name = user.get("firstName") + " " + user.get("lastName");
        Hyperlink postLink;
        if (user.get("email").equals(JwtManager.decodeJwtPayload(JwtManager.getJwtToken()))) {
            postLink = new Hyperlink("your post");
        } else {
            postLink = new Hyperlink(name + "'s post");
        }
        postLink.setTranslateY(-4);
        postLink.setStyle("-fx-text-fill: #1d7754");
        notificationDetail.getChildren().add(2 , postLink);
        Window window = notificationLabel.getScene().getWindow();
        postLink.setOnMouseClicked(event -> PostController.displayPostDialog(post, name, avatarURl, notificationLabel, window));
    }

    private HashMap<String, String> fetchPost(int postId) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/post/" + postId);
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

                        Type type = new TypeToken<HashMap<String, String>>(){}.getType();
                        return gson.fromJson(response.toString(), type);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void searchNotifications() {
        ArrayList<HashMap<String, String>> notifications = fetchNotifications();
        HashSet<String> followingEmails = new HashSet<>(FetcherEmail.fetchEmails("followings", "followed", (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken())));
        for (HashMap<String, String> notification : notifications) {
            String email = notification.get("email");
            if (followingEmails.contains(email)) {
                HashMap<String, String> user = FetcherEmail.fetchProfileDetailsByEmail(email);
                userProfiles.add(user);
                followingNotifications.add(notification);
            }
        }
    }

    private ArrayList<HashMap<String, String>> fetchNotifications() {
        ArrayList<HashMap<String, String>> notifications = new ArrayList<>();
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/notifications");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        Type type = new TypeToken<ArrayList<HashMap<String, String>>>(){}.getType();
                        notifications = gson.fromJson(response.toString(), type);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return notifications;
    }
}
