package org.example.appclient.Controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.example.appclient.util.JwtManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static org.example.appclient.Controllers.FetcherEmail.goToProfile;

public class LikeController {

    private static ArrayList<HashMap<String, String>> likeEmails = new ArrayList<>();

    private static Label tempLabel;

    @FXML
    private Label displayLikeLabel;

    @FXML
    private ScrollPane likeScrollPane;

    @FXML
    private VBox likeVBox;

    public void initialize() {

        likeScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        likeVBox = new VBox();
        likeVBox.setStyle("-fx-background-color: #1b1b1b");
        likeVBox.setPadding(new Insets(10));
        likeScrollPane.setContent(likeVBox);
        likeScrollPane.setStyle("-fx-background-color: #1b1b1b");

        // Set likes label text color to white
        displayLikeLabel.setStyle("-fx-text-fill: white;");

        displayLikes();
    }

    private void displayLikes() {
        displayLikeLabel.setText(likeEmails.size() + " Likes");
        for (HashMap<String, String> likeData : likeEmails) {
            String email = likeData.get("email");
            String likeTime = likeData.get("likeTime");
            String name = likeData.get("userName");
            HashMap<String, String> user = FetcherEmail.fetchProfileDetailsByEmail(email);
            fillDialog(likeTime, email, name, user);
        }
    }

    private void fillDialog(String likeTime, String email, String name, HashMap<String, String> user) {
        String headline = user.get("headline");
        String avatarURL = user.get("avatar_url");

        HBox userBox = new HBox();
        userBox.setStyle("-fx-background-color: #1b1b1b");
        userBox.setSpacing(10);
        userBox.setPrefWidth(543.5);

        ImageView imageView = new ImageView();
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
                    imageView.setOnMouseClicked(event -> FetcherEmail.goToProfile(email, tempLabel));
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Image image = new Image(FetcherEmail.class.getResource("/org/example/appclient/images/linkedInIcon.png").toExternalForm());
            imageView = new ImageView(image);
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            imageView.setOnMouseClicked(event -> FetcherEmail.goToProfile(email, tempLabel));
        }

        Circle circle = new Circle(25, 25, 25);
        imageView.setClip(circle);

        VBox userDetails = new VBox(10);
        Text userName = new Text(name);
        userName.setStyle("-fx-fill: white;");
        Text time = new Text(likeTime);
        time.setStyle("-fx-fill: white;");
        HBox nameAndLikeTime = new HBox();
        nameAndLikeTime.setSpacing(10);
        nameAndLikeTime.getChildren().addAll(userName, time);

        Text userHeadline = new Text(headline);
        userHeadline.setStyle("-fx-fill: white;");

        userDetails.getChildren().addAll(nameAndLikeTime, userHeadline);
        userBox.getChildren().addAll(imageView, userDetails);
        userBox.setPadding(new Insets(10, 10, 10, 10));

        likeVBox.getChildren().add(userBox);
    }

    public static void setLikeEmails(ArrayList<HashMap<String, String>> emails) {
        likeEmails.clear();
        likeEmails.addAll(emails);
    }

    public static void setTempLabel(Label tempLabel) {
        LikeController.tempLabel = tempLabel;
    }
}
