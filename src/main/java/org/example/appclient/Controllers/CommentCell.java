package org.example.appclient.Controllers;

import javafx.application.Platform;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.example.appclient.util.JwtManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class CommentCell extends ListCell<HashMap<String, String>> {
    private final VBox userDetails;
    private final HBox userHBox;
    private final ImageView imageView;
    private final Text nameText;
    private final Text dateText;
    private final TextArea messageArea;

    public CommentCell() {
        userDetails = new VBox(10);
        userHBox = new HBox(10);
        imageView = new ImageView();
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        Circle circle = new Circle(25, 25, 25);
        imageView.setClip(circle);
        nameText = new Text();
        nameText.setFill(Color.WHITE);
        dateText = new Text();
        dateText.setFill(Color.WHITE);
        messageArea = new TextArea();
        messageArea.setStyle("-fx-control-inner-background: #232323;" +
                "-fx-background-color: #232323;" +
                "-fx-border-color: #1d7754;" +
                "-fx-text-fill: white;");
        messageArea.setPrefWidth(300);
        messageArea.setPrefHeight(50);
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setFocusTraversable(false);
        messageArea.getStylesheets().add(getClass().getResource("/org/example/appclient/css/ScrollBar.css").toExternalForm());
        userHBox.getChildren().addAll(imageView, nameText, dateText);
        userDetails.getChildren().addAll(userHBox, messageArea);

        setStyle("-fx-background-color: #1b1b1b;");
    }

    @Override
    protected void updateItem(HashMap<String, String> commentData, boolean empty) {
        super.updateItem(commentData, empty);
        if (empty || commentData == null) {
            setGraphic(null);
        } else {
            String email = commentData.get("email");
            String fullName = commentData.get("name");
            String message = commentData.get("message");
            String commentDate = commentData.get("commentDate");
            String avatarURL = commentData.get("avatarURL");

            nameText.setText(message);
            dateText.setText(commentDate);
            messageArea.setText(fullName);

            if (avatarURL != null && !avatarURL.isEmpty()) {
                new Thread(() -> {
                    try {
                        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080/user/avatar/" + email).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            InputStream inputStream = connection.getInputStream();
                            Image image = new Image(inputStream);
                            Platform.runLater(() -> imageView.setImage(image));
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                Image image = new Image(FetcherEmail.class.getResource("/org/example/appclient/images/defaultAvatar.png").toExternalForm());
                imageView.setImage(image);
            }

            imageView.setOnMouseClicked(event -> FetcherEmail.goToProfile(email, CommentController.tempLabel));

            setGraphic(userDetails);
        }
    }
}
