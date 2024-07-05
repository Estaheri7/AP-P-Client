package org.example.appclient.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;
import org.example.appclient.LinkedInApp;
import org.example.appclient.util.JwtManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FetcherEmail {
    private static final Gson gson = new Gson();

    public static ArrayList<String> fetchEmails(String URL, String type, String requestEmail) {
        ArrayList<String> emails = new ArrayList<>();

        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/" + URL + "//" + requestEmail);
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
                        ArrayList<HashMap<String, String>> datas = gson.fromJson(response.toString(), new TypeToken<List<HashMap<String, String>>>() {
                        }.getType());
                        for (HashMap<String, String> data : datas) {
                            String email = data.get(type);
                            if (email != null) {
                                emails.add(email);
                            }
                        }
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
        return emails;
    }

    public static HashMap<String, String> fetchProfileDetailsByEmail(String email) {
        HttpURLConnection connection = null;
        HashMap<String, String> profileDetails = new HashMap<>();

        if (JwtManager.isJwtTokenAvailable()) {
            try {
                URL url = new URL("http://localhost:8080/user/" + email);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }

                        Type type = new TypeToken<HashMap<String, String>>() {}.getType();
                        profileDetails = gson.fromJson(response.toString(), type);
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

        return profileDetails;
    }

    public static void addEntry(HashMap<String, String> userDetail, VBox userVBox, Label stageLabel) {
        String email = userDetail.get("email");
        String name = userDetail.get("firstName");
        String lastName = userDetail.get("lastName");
        String headline = userDetail.get("headline");
        String city = userDetail.get("city");
        String country = userDetail.get("country");
        String avatarURL = userDetail.get("avatar_url");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefWidth(665);
        scrollPane.setMaxWidth(665);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        HBox entry = new HBox();
        entry.setSpacing(10);
        entry.setPrefWidth(665);
        entry.setPadding(new Insets(10));

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
                    imageView.setOnMouseClicked(event -> goToProfile(email, stageLabel));
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
            imageView.setOnMouseClicked(event -> goToProfile(email, stageLabel));
        }

        Circle circle = new Circle(25, 25, 25);
        imageView.setClip(circle);
        entry.getChildren().add(imageView);

        VBox userDetailsVBox = new VBox();
        userDetailsVBox.setPrefWidth(480);
        Label nameLabel = new Label(name + " " + lastName);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.setStyle("-fx-text-fill: white;");
        Label headlineLabel = new Label(headline);
        headlineLabel.setStyle("-fx-text-fill: white;");
        Label locationLabel = new Label(city + ", " + country);
        locationLabel.setStyle("-fx-text-fill: white;");
        userDetailsVBox.getChildren().addAll(nameLabel, headlineLabel, locationLabel);

        Button button = new Button("Connect");
        if (email.equals(JwtManager.decodeJwtPayload(JwtManager.getJwtToken()))) {
            button.setVisible(false);
        }
        button.setTranslateY(25);
        button.getStylesheets().add(FetcherEmail.class.getResource("/org/example/appclient/css/Button.css").toExternalForm());
        onButton(button, email);

        entry.getChildren().addAll(userDetailsVBox, button);

        scrollPane.setContent(entry);
        userVBox.getChildren().add(scrollPane);
    }

    public static void onButton(Button button, String email) {
        if (ConnectController.isConnectionPending(email)) {
            button.setText("Pending");
            button.setDisable(true);
        } else if (ConnectController.isConnected(email)) {
            button.setText("Message");
        }
        button.setOnAction(event -> {
            if (button.getText().equals("Connect")) {
                ConnectController.setConnectionReceiver(email);
                loadSendConnection(button);
            } else if (button.getText().equals("Message")) {
                // TODO go to message room
                ChatController.openChat(email, button);
            }
        });
    }

    public static void loadSendConnection(Button btn) {
        try {
            FXMLLoader loader = new FXMLLoader(FetcherEmail.class.getResource("/org/example/appclient/sendConnection.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Send Connection");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btn.getScene().getWindow());
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void goToProfile(String email, Label stageLabel) {
        try {
            ProfileController.setProfileEmail(email);

            Parent profilePage = FXMLLoader.load(FetcherEmail.class.getResource("/org/example/appclient/profile.fxml"));
            Stage currentStage = (Stage) stageLabel.getScene().getWindow();
            Scene scene = new Scene(profilePage);

            Stage profileStage = new Stage();
            profileStage.setScene(scene);
            LinkedInApp.setIcon(profileStage);
            profileStage.setTitle("Profile");
            profileStage.initOwner(currentStage);
            profileStage.initModality(Modality.WINDOW_MODAL);

            profileStage.setFullScreen(true);
            profileStage.setFullScreenExitHint("");

            currentStage.hide();
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
