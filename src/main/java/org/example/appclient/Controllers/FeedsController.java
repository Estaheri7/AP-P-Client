package org.example.appclient.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.appclient.LinkedInApp;
import org.example.appclient.util.JwtManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class FeedsController {

    @FXML
    private ImageView avatarImageView;

    @FXML
    private ImageView backgroundImageView;

    @FXML
    private Button addPostButton;

    @FXML
    private HBox connectionsHbox;

    @FXML
    private HBox followersHbox;

    @FXML
    private HBox followingHbox;

    @FXML
    private Label headlineLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label connectionsLabel;
    @FXML
    private Label followersLabel;
    @FXML
    private Label followingLabel;

    @FXML
    private VBox  mainContainer;

    @FXML
    private ScrollPane connecionsScrollPane;

    @FXML
    private VBox connectionsVbox;

    @FXML
    private TextField searchTextField;

    private ArrayList<HashMap<String, String>> followingPosts = new ArrayList<>();

    private String avatarURL = "";

    private ListView<HashMap<String, String>> postListView;

    private String searchKey;

    @FXML
    void onAddPostButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/appclient/addPost.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Post");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addPostButton.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setResizable(false);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
        addPostButton.getStylesheets().addAll(getClass().getResource("/org/example/appclient/css/button.css").toExternalForm());

        Circle clip = new Circle(27, 27, 27);
        avatarImageView.setClip(clip);

        // Setting fixed width and height for backgroundImageView
//        backgroundImageView.setFitWidth(483);
//        backgroundImageView.setFitHeight(138);
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setSmooth(true);

        Rectangle rectangleClip = new Rectangle(626, 176);
        rectangleClip.setArcHeight(10);
        rectangleClip.setArcWidth(10);
        backgroundImageView.setClip(rectangleClip);

        backgroundImageView.setStyle("-fx-min-width: 483;" +
                "-fx-min-height: 138;" +
                "-fx-max-width: 483;" +
                "-fx-max-height: 138;");

        connectionsHbox.setOnMouseClicked(event -> loadPage("connections.fxml"));
//        connectionsHbox.getStylesheets().add(getClass().getResource("/org/example/appclient/css/Hbox.css").toExternalForm());
        followersHbox.setOnMouseClicked(event -> loadPage("followers.fxml"));
//        followersHbox.getStylesheets().add(getClass().getResource("/org/example/appclient/css/Hbox.css").toExternalForm());
        followingHbox.setOnMouseClicked(event -> loadPage("following.fxml"));
//        followingHbox.getStylesheets().add(getClass().getResource("/org/example/appclient/css/Hbox.css").toExternalForm());
        fillProfile();

        postListView = new ListView<>();
        postListView.requestFocus();
        postListView.getStylesheets().add(getClass().getResource("/org/example/appclient/css/ScrollBar.css").toExternalForm());
        postListView.setStyle("-fx-background-color: #232323FF");
        postListView.setPrefHeight(700);
        postListView.setCellFactory(param -> new PostCell());
        mainContainer.setPrefHeight(600);
        mainContainer.getChildren().add(postListView);
        searchTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                new Thread(() -> displayPosts(search())).start();
            }
        });

        new Thread(() -> fillFollowingPosts()).start();
        Platform.runLater(() -> displayPosts(followingPosts));

        connectionsVbox = new VBox();
        connectionsVbox.setStyle("-fx-background-color: #232323");
        connecionsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        connecionsScrollPane.setContent(connectionsVbox);
        displayConnections();
    }

    private void fillProfile() {

        String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());

        HashMap<String , String> user = FetcherEmail.fetchProfileDetailsByEmail(email);


        nameLabel.setText(user.get("firstName") + " " + user.get("lastName"));

        headlineLabel.setText(user.get("headline"));

        followersLabel.setText(user.get("followers"));

        followingLabel.setText( user.get("following"));

        connectionsLabel.setText(user.get("connections"));

        avatarURL = user.get("avatar_url");
        if (avatarURL != null && !avatarURL.isEmpty()) {
            try {
                URL url = new URL("http://localhost:8080/user/avatar/" + email);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();

                    Image image = new Image(inputStream);
                    avatarImageView.setImage(image);
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Image image = new Image(getClass().getResource("/org/example/appclient/images/defaultAvatar.png").toExternalForm());
            avatarImageView.setImage(image);
        }

        avatarImageView.setOnMouseClicked(event -> FetcherEmail.goToProfile(email, nameLabel));

        String backgroundUrl = user.get("background_url");
        if (backgroundUrl != null && !backgroundUrl.isEmpty()) {
            try {
                URL url = new URL("http://localhost:8080/user/background/" + email);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    Image image = new Image(inputStream);
                    backgroundImageView.setImage(image);
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int connections = Integer.parseInt(user.get("connections"));
        int followers = Integer.parseInt(user.get("followers"));
        int following = Integer.parseInt(user.get("following"));
        connectionsLabel.setText(connections + " connections");
        followersLabel.setText(followers + " followers");
        followingLabel.setText(following + " following");
        headlineLabel.setText((user.get("headline")));
    }

    private void displayPosts(ArrayList<HashMap<String, String>> posts) {
        Platform.runLater(() -> {
            PostCell.setTempLabel(nameLabel);
            postListView.getItems().clear();
            postListView.getItems().addAll(posts);
        });
    }

    public void displayConnections() {
        ArrayList<String> senderEmails = FetcherEmail.fetchEmails("connections", "sender", ProfileController.getProfileEmail());
        connectionsVbox.getChildren().clear();
        for (String senderEmail : senderEmails) {
            HashMap<String, String> userDetail = FetcherEmail.fetchProfileDetailsByEmail(senderEmail);
            FetcherEmail.addEntry(userDetail, connectionsVbox , connectionsLabel);
        }
    }

    private ArrayList<HashMap<String, String>> search() {
        setSearch();
        ArrayList<HashMap<String, String>> posts = new ArrayList<>();
        ArrayList<HashMap<String, String>> allposts = PostController.fetchPostFeeds();
        for (HashMap<String, String> post : allposts) {
            if (searchKey.isEmpty()) {
                return allposts;
            }
            if (post.get("content").contains(searchKey)) {
                posts.add(post);
            }
        }
        return posts;
    }

    private void setSearch(){
        searchKey = searchTextField.getText();
    }

    private void loadPage(String fxml) {
        try {
            Parent loader = FXMLLoader.load(getClass().getResource("/org/example/appclient/" + fxml));

            Scene pageScene = new Scene(loader);
            Stage currentStage = (Stage) headlineLabel.getScene().getWindow();

            Stage newStage = new Stage();
            newStage.setScene(pageScene);
            LinkedInApp.setIcon(newStage);
            newStage.initOwner(currentStage);
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.setFullScreen(true);
            newStage.setFullScreenExitHint("");
            currentStage.hide();
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillFollowingPosts() {
        String selfEmail = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());
        ArrayList<String> followingEmails = FetcherEmail.fetchEmails("followings", "followed", selfEmail);
        ArrayList<HashMap<String, String>> selfPosts = PostController.fetchPostFromUser(selfEmail);
        ArrayList<HashMap<String, String>> posts = PostController.fetchPostFeeds();
        for (String followingEmail : followingEmails) {
            for (HashMap<String, String> post : posts) {
                if (post.get("author").equals(followingEmail)) {
                    followingPosts.add(post);
                }
            }
        }

        followingPosts.addAll(selfPosts);
    }
}
