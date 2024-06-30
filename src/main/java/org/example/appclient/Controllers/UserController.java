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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
import java.util.ArrayList;
import java.util.HashMap;

public class UserController {

    private final Gson gson = new Gson();

    private static String searchKey;

    @FXML
    private Label usersLabel;

    @FXML
    private VBox usersVBox;

    @FXML
    private ScrollPane userScrollPane;

    private ArrayList<HashMap<String, String>> fetchAllUsers() {
        HttpURLConnection connection = null;
        ArrayList<HashMap<String, String>> users = new ArrayList<>();
        try {
            URL url = new URL("http://localhost:8080/users");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    Type type = new TypeToken<ArrayList<HashMap<String, String>>>(){}.getType();
                    users = gson.fromJson(response.toString(), type);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    private ArrayList<HashMap<String, String>> search() {
        ArrayList<HashMap<String, String>> users = new ArrayList<>();
        ArrayList<HashMap<String, String>> currentUsers = fetchAllUsers();
        for (HashMap<String, String> user : currentUsers) {
            if (searchKey.isEmpty()) {
                return currentUsers;
            }
            if (user.get("firstName").contains(searchKey) || user.get("lastName").contains(searchKey)) {
                users.add(user);
            }
        }
        return users;
    }

    private void displayUsers(HashMap<String, String> user) {
        String email = user.get("email");
        String firstName = user.get("firstName");
        String lastName = user.get("lastName");
        String headline = user.get("headline");
        String country = user.get("country");
        String city = user.get("city");
        String avatar = user.get("avatar_url");

        HBox userEntry = new HBox();
        userEntry.setSpacing(10);
        userEntry.setPrefWidth(650);


        ImageView imageView = new ImageView();
        if (avatar != null && !avatar.isEmpty()) {
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
                    imageView.setOnMouseClicked(event -> goToProfile(email));
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Image image = new Image(getClass().getResource("/org/example/appclient/images/linkedInIcon.png").toExternalForm());
            imageView = new ImageView(image);
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            imageView.setOnMouseClicked(event -> goToProfile(email));
        }

        Circle circle = new Circle(25, 25, 25);
        imageView.setClip(circle);
        userEntry.getChildren().add(imageView);

        VBox userDetailsVBox = new VBox();
        userDetailsVBox.setPrefWidth(480);
        Label nameLabel = new Label(firstName + " " + lastName);
        nameLabel.setFont(new Font(18));
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        Label headlineLabel = new Label(headline);
        Label locationLabel = new Label(city + ", " + country);
        userDetailsVBox.getChildren().addAll(nameLabel, headlineLabel, locationLabel);
        //        int nameSize = (name + lastName).length();
        Button button = new Button("Connect");
        button.setTranslateY(25);
        button.setStyle("-fx-background-color: #1d7754; -fx-text-fill: white");
//        messageButton.setTranslateX(nameSize * 7 + 300);
        onButton(button, email);
        userEntry.setPadding(new Insets(10, 10, 10, 10));
        userEntry.getChildren().addAll(userDetailsVBox, button);
        usersVBox.getChildren().add(userEntry);
    }

    private void onButton(Button button, String email) {
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
                System.out.println("Go to message");
            }
        });
    }

    private void loadSendConnection(Button btn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/appclient/sendConnection.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("send connection");
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

    private void goToProfile(String email) {
        try {
            ProfileController.setProfileEmail(email);

            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/example/appclient/profile.fxml"));
            Stage currentStage = (Stage) usersLabel.getScene().getWindow();
            Scene scene = new Scene(profilePage);

            Stage profileStage = new Stage();
            profileStage.setScene(scene);
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

    public void initialize() {
        usersVBox = new VBox();
        userScrollPane.setContent(usersVBox);
        display();
    }

    private void display() {
        ArrayList<HashMap<String, String>> users = search();
        usersVBox.getChildren().clear();
        for (HashMap<String, String> user : users) {
            displayUsers(user);
        }
    }

    public static void setSearchKey(String searchKey) {
        UserController.searchKey = searchKey;
    }

}
