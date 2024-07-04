package org.example.appclient.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.appclient.util.JwtManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class TopBarController {

    private final Gson gson = new Gson();
    @FXML
    private ImageView homeImageView;

    @FXML
    private ImageView usersImageView;

    @FXML
    private ImageView meImageView;

    @FXML
    private ImageView messagingImageView;

    @FXML
    private ImageView myNetworkImageView;

    @FXML
    private TextField searchTextField;

    @FXML
    private ImageView logOutImage;

    @FXML
    private ImageView notificationImageView;

    private void onHomeClicked() {
        loadStage("Feeds.fxml");
    }

    private void search(){
        String searchKey = searchTextField.getText();
        UserController.setSearchKey(searchKey);
        loadStage("users.fxml");
    }

    private void onUsersClicked() {
        UserController.setSearchKey("");
        loadStage("users.fxml");
    }

    private void onMeClicked() {
        loadStage("profile.fxml");
    }

    private void onMessagingClicked() {
        loadStage("messagePage.fxml");
    }

    private void onMyNetworkClicked() {
        loadStage("network.fxml");
    }

    private void onNotificationClicked() {
        loadStage("notificationsPage.fxml");
    }

    private void loadStage(String fxml){
        try {
            Parent targetPage = FXMLLoader.load(getClass().getResource("/org/example/appclient/" + fxml));
            Scene targetPageScene = new Scene(targetPage);
            Stage currentStage = (Stage) meImageView.getScene().getWindow();

            Stage targetStage = new Stage();
            targetStage.setScene(targetPageScene);
            targetStage.initOwner(currentStage);
            targetStage.initModality(Modality.APPLICATION_MODAL);
            targetStage.setFullScreen(true);
            targetStage.setFullScreenExitHint("");
            currentStage.hide();
            targetStage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String fetchProfileDetailsByEmail() {
        HttpURLConnection connection = null;
        HashMap<String, String> profileDetails = new HashMap<>();

        if (JwtManager.isJwtTokenAvailable()) {
            try {
                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());
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

        return profileDetails.get("avatar_url");
    }

    private void fillAvatar(String avatar) {
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());

                URL url = new URL("http://localhost:8080/user/avatar/" + email);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                int responseCode = connection.getResponseCode();
                System.out.println(responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();

                    Image image = new Image(inputStream);
                    meImageView.setImage(image);
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Image image = new Image(getClass().getResource("/org/example/appclient/images/linkedInIcon.png").toExternalForm());
            meImageView.setImage(image);
        }
    }

    public void initialize() {

        String avatar = fetchProfileDetailsByEmail();
        fillAvatar(avatar);

        Circle circle = new Circle(14, 14, 14);
        meImageView.setPreserveRatio(false);
        meImageView.setClip(circle);

        meImageView.setOnMouseClicked(event -> onMeClicked());

        usersImageView.setOnMouseClicked(event -> onUsersClicked());

        searchTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                search();
            }
        });

        logOutImage.setOnMouseClicked(event -> {
            loadStage("login.fxml");
        });

        homeImageView.setOnMouseClicked(event -> onHomeClicked());

        myNetworkImageView.setOnMouseClicked(event -> onMyNetworkClicked());

        messagingImageView.setOnMouseClicked(event -> onMessagingClicked());

        notificationImageView.setOnMouseClicked(event -> onNotificationClicked());
    }
}
