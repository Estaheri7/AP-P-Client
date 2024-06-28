package org.example.appclient.Controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.appclient.util.JwtManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class NetWorkController {

    @FXML
    private ScrollPane userScrollPane;

    @FXML
    private VBox usersVBox;

    @FXML
    private Label usersLabel;


    private void displayRequests() {
        String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());
        ArrayList<String> users = FetcherEmail.fetchEmails("connections/receiver", "sender", email);
        usersVBox.getChildren().clear();
        for (String user : users) {
            HashMap<String, String> userDetail = FetcherEmail.fetchProfileDetailsByEmail(user);
            addEntry(userDetail);
        }
    }

    public void addEntry(HashMap<String, String> userDetail) {
        String email = userDetail.get("email");
        String name = userDetail.get("firstName");
        String lastName = userDetail.get("lastName");
        String headline = userDetail.get("headline");
        String city = userDetail.get("city");
        String country = userDetail.get("country");
        String avatarURL = userDetail.get("avatar_url");

        HBox entry = new HBox();
        entry.setSpacing(10);
        entry.setPrefWidth(650);

        if (avatarURL != null && !avatarURL.isEmpty()) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080/user/avatar/" + email).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    Image image = new Image(inputStream);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setOnMouseClicked(event -> FetcherEmail.goToProfile(email, usersLabel));
                    entry.getChildren().add(imageView);
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Image image = new Image(FetcherEmail.class.getResource("/org/example/appclient/images/linkedInIcon.png").toExternalForm());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            imageView.setOnMouseClicked(event -> FetcherEmail.goToProfile(email, usersLabel));
            entry.getChildren().add(imageView);
        }

        VBox userDetailsVBox = new VBox();
        userDetailsVBox.setPrefWidth(480);
        Label nameLabel = new Label(name + " " + lastName);
        nameLabel.setFont(new Font(18));
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        Label headlineLabel = new Label(headline);
        Label locationLabel = new Label(city + ", " + country);
        userDetailsVBox.getChildren().addAll(nameLabel, headlineLabel, locationLabel);

        Button acceptButton;
        Button declineButton;

        acceptButton = new Button("Accept");
        declineButton = new Button("Decline");
        acceptButton.setPrefWidth(250);
        declineButton.setPrefWidth(250);
        acceptButton.setTranslateY(25);
        declineButton.setTranslateY(25);

        acceptButton.setOnAction(event -> acceptConnection(acceptButton, declineButton,email));
        declineButton.setOnAction(event -> declineConnection(acceptButton, declineButton, email));

        entry.setPadding(new Insets(10, 10, 10, 10));
        entry.getChildren().addAll(userDetailsVBox, acceptButton, declineButton);
        usersVBox.getChildren().add(entry);
    }

    private void acceptConnection(Button acceptButton, Button declineButton, String sender) {
        acceptButton.setVisible(false);
        declineButton.setVisible(false);
        ConnectController.acceptConnection(sender);
    }

    private void declineConnection(Button acceptButton, Button declineButton, String sender) {
        acceptButton.setVisible(false);
        declineButton.setVisible(false);
        ConnectController.declineConnection(sender);
    }

    public void initialize() {
        usersVBox = new VBox();
        userScrollPane.setContent(usersVBox);
        displayRequests();
    }
}
