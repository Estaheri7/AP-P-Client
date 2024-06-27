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

    private void onHomeClicked() {}
    private void search(){
        String searchKey = searchTextField.getText();
        UserController.setSearchKey(searchKey);
        loadStage();

    }

    private void loadStage(){
        try {
            Parent usersPage = FXMLLoader.load(getClass().getResource("/org/example/appclient/users.fxml"));
            Scene userPageScene = new Scene(usersPage);
            Stage currentStage = (Stage) meImageView.getScene().getWindow();

            Stage userStage = new Stage();
            userStage.setScene(userPageScene);
            userStage.initOwner(currentStage);
            userStage.initModality(Modality.APPLICATION_MODAL);
            userStage.setFullScreen(true);
            userStage.setFullScreenExitHint("");
            currentStage.hide();
            userStage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onUsersClicked() {

        UserController.setSearchKey("");
    try {
        Parent usersPage = FXMLLoader.load(getClass().getResource("/org/example/appclient/users.fxml"));
        Scene userPageScene = new Scene(usersPage);
        Stage currentStage = (Stage) meImageView.getScene().getWindow();

        Stage userStage = new Stage();
        userStage.setScene(userPageScene);
        userStage.initOwner(currentStage);
        userStage.initModality(Modality.APPLICATION_MODAL);
        userStage.setFullScreen(true);
        userStage.setFullScreenExitHint("");
        currentStage.hide();
        userStage.show();
    }
        catch (IOException e) {
        e.printStackTrace();
    }

    }
    private void onMeClicked() {
        try {
            String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());
            ProfileController.setProfileEmail(email);

            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/example/appclient/profile.fxml"));
            Scene profilePageScene = new Scene(profilePage);
            Stage currentStage = (Stage) meImageView.getScene().getWindow();

            Stage profileStage = new Stage();
            profileStage.setScene(profilePageScene);
            profileStage.initOwner(currentStage);
            profileStage.initModality(Modality.APPLICATION_MODAL);
            profileStage.setFullScreen(true);
            profileStage.setFullScreenExitHint("");
            currentStage.hide();
            profileStage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void onMessagingClicked() {}
    private void onMyNetworkClicked() {}

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
        meImageView.setOnMouseClicked(event -> onMeClicked());
        usersImageView.setOnMouseClicked(event -> onUsersClicked());
        searchTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                search();
            }
        });
    }
}
