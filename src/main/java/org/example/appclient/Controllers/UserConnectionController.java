package org.example.appclient.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
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


public class UserConnectionController {

    private final Gson gson = new Gson();

    @FXML
    private ScrollPane connectionsScrollPane;

    @FXML
    private TextField searchByName;

    @FXML
    private VBox connectionsVBox;

    @FXML
    private Label connectionLabel;


    public ArrayList<String> fetchSenderEmails() {
        ArrayList<String> senderEmails = new ArrayList<>();

        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/connections/" + ProfileController.getProfileEmail());
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
                        ArrayList<HashMap<String, String>> connections = gson.fromJson(response.toString(), new TypeToken<List<HashMap<String, String>>>(){}.getType());
                        for (HashMap<String, String> connect : connections) {
                            String sender = connect.get("sender");
                            if (sender != null) {
                                senderEmails.add(sender);
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
        return senderEmails;
    }

    public HashMap<String, String> fetchProfileDetailsByEmail(String senderEmail) {
        HttpURLConnection connection = null;
        HashMap<String, String> profileDetails = new HashMap<>();

        if (JwtManager.isJwtTokenAvailable()) {
            try {
                URL url = new URL("http://localhost:8080/user/" + senderEmail);
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

    public void displayConnections() {
        ArrayList<String> senderEmails = fetchSenderEmails();
        connectionLabel.setText(senderEmails.size() + " Connection");
        connectionsVBox.getChildren().clear();
        for (String senderEmail : senderEmails) {
            HashMap<String, String> userDetail = fetchProfileDetailsByEmail(senderEmail);
            addConnectionEntry(userDetail);
        }
    }

    private void addConnectionEntry(HashMap<String, String> userDetail) {
        String email = userDetail.get("email");
        String name = userDetail.get("firstName");
        String lastName = userDetail.get("lastName");
        String headline = userDetail.get("headline");
        String city = userDetail.get("city");
        String country = userDetail.get("country"); // Assuming country is part of the userDetail map
        String avatarURL = userDetail.get("avatar_url");

        HBox connectionEntry = new HBox();
        connectionEntry.setSpacing(10);
        connectionEntry.setPrefWidth(650);

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
                    imageView.setOnMouseClicked(event -> goToProfile(email));
                    connectionEntry.getChildren().add(imageView);
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        VBox userDetailsVBox = new VBox();
        userDetailsVBox.setPrefWidth(480);
        Label nameLabel = new Label(name + " " + lastName);
        nameLabel.setFont(new Font(18));
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        Label headlineLabel = new Label(headline);
        Label locationLabel = new Label(city + ", " + country);
        userDetailsVBox.getChildren().addAll(nameLabel, headlineLabel, locationLabel);

//        int nameSize = (name + lastName).length();
        Button messageButton = new Button("Message");
        messageButton.setTranslateY(25);
//        messageButton.setTranslateX(nameSize * 7 + 300);
        messageButton.setOnAction(event -> {
            // TODO go to chat page
            System.out.println("Message button clicked for: " + email);
        });
        connectionEntry.setPadding(new Insets(10, 10, 10, 10));
        connectionEntry.getChildren().addAll(userDetailsVBox, messageButton);
        connectionsVBox.getChildren().add(connectionEntry);
    }

    public void initialize() {
        connectionsVBox = new VBox();
        connectionsScrollPane.setContent(connectionsVBox);
        displayConnections();
    }

    private void goToProfile(String email) {
        try {
            ProfileController.setProfileEmail(email);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/appclient/profile.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) searchByName.getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setFullScreen(true);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

