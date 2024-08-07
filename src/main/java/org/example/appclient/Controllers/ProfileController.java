package org.example.appclient.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.appclient.LinkedInApp;
import org.example.appclient.util.JwtManager;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class ProfileController {

    private final Gson gson = new Gson();
    private static String profileEmail;

    @FXML
    private DialogPane dialogPane;

    @FXML
    private VBox mainContainer;

    private ListView<HashMap<String, String>> postListView;

    @FXML
    private Button addPostButton;

    @FXML
    private Label userPostLabel;

    @FXML
    private VBox postVBox;

    @FXML
    private TextField FieldTextField;

    @FXML
    private TextField workNumberTextField;

    @FXML
    private TextField homeNumberTextField;

    @FXML
    private TextField addressTextField;

    @FXML
    private Button allEducationsButton;

    @FXML
    private ImageView avatarImageView;

    @FXML
    private Button backgroundButton;

    @FXML
    private ImageView backgroundImageView;

    @FXML
    private TextField birthDateTextField;

    @FXML
    private TextField communityTextField;

    @FXML
    private Button connectButton;

    @FXML
    private Hyperlink connectionsLink;

    @FXML
    private Button contactUpdateButton;

    @FXML
    private TextField descriptionTextField;

    @FXML
    private Button editButton;

    @FXML
    private Button educationAddButton;

    @FXML
    private Button educationUpdateButton;

    @FXML
    private TextField endDateTextField;

    @FXML
    private Hyperlink fastConnectLink;

    @FXML
    private Button followButton;

    @FXML
    private Hyperlink followersLink;

    @FXML
    private Hyperlink followingLink;

    @FXML
    private TextField gradeTextField;

    @FXML
    private Label headlineLabel;

    @FXML
    private Label locationLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private static Button tempButton;

    @FXML
    private TextField phoneNumberTextField;

    @FXML
    private TextField schoolNameTextField;

    @FXML
    private TextField skill1TextField;

    @FXML
    private TextField skill2TextField;

    @FXML
    private TextField skill3TextField;

    @FXML
    private TextField skill4TextField;

    @FXML
    private TextField skill5TextField;

    @FXML
    private Button skillsUpdateButton;

    @FXML
    private TextField startDateTextField;

    @FXML
    private Hyperlink viewLinkHyperLink;

    @FXML
    private Label communityLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private RadioButton onlyMeRadioButton;

    @FXML
    private RadioButton myConnectionRadioButton;

    @FXML
    private RadioButton everyoneRadioButton;

    private ToggleGroup toggleGroup;

    private String avatarURL = "";

    private int currentPage = 0;

    private static final int POSTS_PER_PAGE = 3;

    @FXML
    void onAllEducationsButton(ActionEvent event) {
        try {
            AllEducationsController.setProfileEmail(profileEmail);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/appclient/displayEducations.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Educations");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(allEducationsButton.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setResizable(false);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onBackgroundButton(ActionEvent event) {
        uploadImage("background");
    }

    @FXML
    void onConnectButton(ActionEvent event) {
        String buttonText = connectButton.getText();
        if (buttonText.equals("Connect")) {
            loadSendConnection();
        } else if (buttonText.equals("Message")) {
            ChatController.openChat(profileEmail, connectButton);
        }
    }

    private void loadSendConnection() {
        try {
            ConnectController.setConnectionReceiver(profileEmail);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/appclient/sendConnection.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("send connection");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(connectButton.getScene().getWindow());
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onConnectionsLink(ActionEvent event) {
        try {
            Parent connectionsPage = FXMLLoader.load(getClass().getResource("/org/example/appclient/connections.fxml"));

            Stage currentStage = (Stage) connectionsLink.getScene().getWindow();
            Scene scene = new Scene(connectionsPage);
            Stage connectionsStage = new Stage();
            LinkedInApp.setIcon(connectionsStage);
            connectionsStage.setScene(scene);
            connectionsStage.setTitle("connections");
            connectionsStage.initOwner(currentStage);
            connectionsStage.initModality(Modality.WINDOW_MODAL);

            connectionsStage.setFullScreen(true);
            connectionsStage.setFullScreenExitHint("");

            currentStage.hide();
            connectionsStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onContactUpdateButton(ActionEvent event) {
        try {
            UpdateSkillController.setProfileEmail(profileEmail);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/appclient/contactUpdate.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Update Contact");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(skillsUpdateButton.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setResizable(false);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onEditButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/appclient/introEdit.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Profile");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(editButton.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setResizable(false);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onEducationAddButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/appclient/addEducation.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Education");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(educationAddButton.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setResizable(false);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onEducationUpdateButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/appclient/updateEducation.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Update Education");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(educationUpdateButton.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setResizable(false);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onFastConnectLink(ActionEvent event) {

    }

    @FXML
    void onFollowButton(ActionEvent event) {
        String buttonText = followButton.getText();
        if (buttonText.equals("Follow")) {
            FollowController.follow(profileEmail);
            followButton.setText("Unfollow");
            // TODO display increased number
        } else if (buttonText.equals("Unfollow")) {
            FollowController.unfollow(profileEmail);
            followButton.setText("Follow");
            // TODO display decreased number
        }
    }

    @FXML
    void onFollowersLink(ActionEvent event) {
        try {
            Parent followersRoot = FXMLLoader.load(getClass().getResource("/org/example/appclient/followers.fxml"));
            Scene followerScene = new Scene(followersRoot);

            Stage currentStage = (Stage) followersLink.getScene().getWindow();
            Stage followersStage = new Stage();
            followersStage.setScene(followerScene);
            LinkedInApp.setIcon(followersStage);
            followersStage.setTitle("Followers");
            followersStage.initOwner(currentStage);
            followersStage.initModality(Modality.WINDOW_MODAL);
            followersStage.setFullScreen(true);
            followersStage.setFullScreenExitHint("");

            currentStage.hide();
            followersStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onFollowingLink(ActionEvent event) {
        try {
            Parent followingRoot = FXMLLoader.load(getClass().getResource("/org/example/appclient/following.fxml"));
            Scene followingScene = new Scene(followingRoot);

            Stage currentStage = (Stage) followingLink.getScene().getWindow();
            Stage followingStage = new Stage();
            followingStage.setScene(followingScene);
            LinkedInApp.setIcon(followingStage);
            followingStage.setTitle("Following");
            followingStage.initOwner(currentStage);
            followingStage.initModality(Modality.WINDOW_MODAL);
            followingStage.setFullScreen(true);
            followingStage.setFullScreenExitHint("");

            currentStage.hide();
            followingStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onSkillsUpdateButton(ActionEvent event) {
        try {
            UpdateSkillController.setProfileEmail(profileEmail);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/appclient/updateSkill.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Update Skills");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(skillsUpdateButton.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setResizable(false);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onViewLinkHyperLink(ActionEvent event) {

    }

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
        // styles
        addPostButton.getStylesheets().add(getClass().getResource("/org/example/appclient/css/Button.css").toExternalForm());
        editButton.getStylesheets().add(getClass().getResource("/org/example/appclient/css/Button.css").toExternalForm());
        educationUpdateButton.getStylesheets().add(getClass().getResource("/org/example/appclient/css/Button.css").toExternalForm());
        allEducationsButton.getStylesheets().add(getClass().getResource("/org/example/appclient/css/Button.css").toExternalForm());
        educationAddButton.getStylesheets().add(getClass().getResource("/org/example/appclient/css/Button.css").toExternalForm());
        backgroundButton.getStylesheets().add(getClass().getResource("/org/example/appclient/css/Button.css").toExternalForm());
        followButton.getStylesheets().add(getClass().getResource("/org/example/appclient/css/Button.css").toExternalForm());
        connectButton.getStylesheets().add(getClass().getResource("/org/example/appclient/css/Button.css").toExternalForm());
        skillsUpdateButton.getStylesheets().add(getClass().getResource("/org/example/appclient/css/Button.css").toExternalForm());
        contactUpdateButton.getStylesheets().add(getClass().getResource("/org/example/appclient/css/Button.css").toExternalForm());

        avatarImageView.setOnMouseClicked(event -> uploadImage("avatar"));
        avatarImageView.setFitWidth(142);
        avatarImageView.setFitHeight(142);

        avatarImageView.setPreserveRatio(false);
        avatarImageView.setSmooth(true);
        avatarImageView.setStyle("-fx-min-width: 142;" +
                "-fx-min-height: 142");

        Circle clip = new Circle(71, 71, 71);
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

        // privacy contact
        toggleGroup = new ToggleGroup();
        onlyMeRadioButton.setToggleGroup(toggleGroup);
        myConnectionRadioButton.setToggleGroup(toggleGroup);
        everyoneRadioButton.setToggleGroup(toggleGroup);

//        onlyMeRadioButton.setSelected(true);
//        myConnectionRadioButton.setSelected(false);
//        everyoneRadioButton.setSelected(false);

        onlyMeRadioButton.setOnAction(this::handlePrivacyRadioButtonAction);
        myConnectionRadioButton.setOnAction(this::handlePrivacyRadioButtonAction);
        everyoneRadioButton.setOnAction(this::handlePrivacyRadioButtonAction);

        fillProfile();


        Platform.runLater(() -> {
            postListView = new ListView<>();
            postListView.setStyle("-fx-background-color: #232323FF; -fx-background-radius: 10;");
            postListView.setPrefHeight(700);
            PostCell.setTempLabel(userPostLabel);
            postListView.setCellFactory(param -> new PostCell());
            mainContainer.getChildren().add(postListView);
            displayPosts();
        });
    }

    private void displayPosts() {
        String name = nameLabel.getText();
        ArrayList<HashMap<String, String>> posts = PostController.fetchPostFromUser(profileEmail);
        if (!posts.isEmpty()) {
            userPostLabel.setText(name + "'s Posts");
        } else {
            userPostLabel.setText("No Posts");
        }
        postListView.getItems().clear();
        postListView.getItems().addAll(posts);
    }

    private void handlePrivacyRadioButtonAction(ActionEvent event) {
        RadioButton selectedRadioButton = (RadioButton) toggleGroup.getSelectedToggle();
        String privacySetting = selectedRadioButton.getText();

        String visibility = null;

        if (privacySetting.equals("Only Me")) {
            visibility = "only_me";
        } else if (privacySetting.equals("My Connections")) {
            visibility = "my_connections";
        } else if (privacySetting.equals("Everyone")) {
            visibility = "everyone";
        }

        changeVisibility(visibility);
    }

    private void changeVisibility(String visibility) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());

                URL url = new URL("http://localhost:8080/profile/contact/visibility/" + email);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("AUTHORIZATION", "Bearer " + JwtManager.getJwtToken());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                HashMap<String, String> visibilityHashMap = new HashMap<>();
                visibilityHashMap.put("visibility", visibility);

                String jsonData = gson.toJson(visibilityHashMap, HashMap.class);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonData.getBytes());
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (!(responseCode == HttpURLConnection.HTTP_OK)) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                        System.out.println(br.readLine());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setVisibilityRadioButton(String visibility) {
        if (visibility.equals("only_me")) {
            onlyMeRadioButton.setSelected(true);
        } else if (visibility.equals("my_connections")) {
            myConnectionRadioButton.setSelected(true);
        } else if (visibility.equals("everyone")) {
            everyoneRadioButton.setSelected(true);
        }
    }

    private void fillProfile() {
        if (ConnectController.isConnectionPending(profileEmail)) {
            connectButton.setText("Pending...");
            connectButton.setDisable(true);
        } else if (ConnectController.isConnected(profileEmail)) {
            connectButton.setText("Message");
            connectButton.setDisable(false);
        }
        if (FollowController.hasFollowed(profileEmail)) {
            followButton.setText("Unfollow");
        }

        HashMap<String, Object> profile = fetchUserProfile();

        Type hashmapType = new TypeToken<HashMap<String, Object>>(){}.getType();
        HashMap<String, Object> user = gson.fromJson(gson.toJson(profile.get("user")), hashmapType);
        HashMap<String, Object> skills = gson.fromJson(gson.toJson(profile.get("skill")), hashmapType);
        HashMap<String, Object> contact = gson.fromJson(gson.toJson(profile.get("contact")), hashmapType);

        Type educationListType = new TypeToken<ArrayList<HashMap<String, Object>>>() {}.getType();
        ArrayList<HashMap<String, Object>> educations = gson.fromJson(gson.toJson(profile.get("educations")), educationListType);

        HashMap<String, Object> currentEducation = null;
        if (educations != null && !educations.isEmpty()) {
            currentEducation = educations.get(educations.size() - 1);
        }

        // set main profile
        nameLabel.setText(user.get("firstName") + " " + user.get("lastName"));
        headlineLabel.setText((String) user.get("headline"));
        locationLabel.setText(user.get("city") + ", " + user.get("country"));

        // set avatar
        avatarURL = (String) user.get("avatar_url");
        if (avatarURL != null && !avatarURL.isEmpty()) {
            try {
                URL url = new URL("http://localhost:8080/user/avatar/" + profileEmail);
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

        // set background
        String backgroundUrl = (String) user.get("background_url");
        if (backgroundUrl != null && !backgroundUrl.isEmpty()) {
            try {
                URL url = new URL("http://localhost:8080/user/background/" + profileEmail);
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

        int connections = ((Double) user.get("connections")).intValue();
        int followers = ((Double) user.get("followers")).intValue();
        int following = ((Double) user.get("following")).intValue();
        connectionsLink.setText(connections + " connections");
        followersLink.setText(followers + " followers");
        followingLink.setText(following + " following");

        // set current education
        schoolNameTextField.setText((String) currentEducation.get("schoolName"));
        FieldTextField.setText((String) currentEducation.get("field"));
        gradeTextField.setText(currentEducation.get("grade").toString());
        startDateTextField.setText((String) currentEducation.get("startDate"));
        endDateTextField.setText((String) currentEducation.get("endDate"));
        if (currentEducation.get("community") == null || currentEducation.get("community").toString().isEmpty()) {
            communityLabel.setVisible(false);
            communityTextField.setVisible(false);
        }
        if (currentEducation.get("community") == null || currentEducation.get("description").toString().isEmpty()) {
            descriptionLabel.setVisible(false);
            descriptionTextField.setVisible(false);
        }
        communityTextField.setText((String) currentEducation.get("community"));
        descriptionTextField.setText((String) currentEducation.get("description"));

        // set skills
        skill1TextField.setText((String) skills.get("skill1"));
        skill2TextField.setText((String) skills.get("skill2"));
        skill3TextField.setText((String) skills.get("skill3"));
        skill4TextField.setText((String) skills.get("skill4"));
        skill5TextField.setText((String) skills.get("skill5"));

        // set contacts
        phoneNumberTextField.setText((String) contact.get("phoneNumber"));
        workNumberTextField.setText((String) contact.get("workNumber"));
        homeNumberTextField.setText((String) contact.get("homeNumber"));

        addressTextField.setText((String) contact.get("address"));
        String visibility = (String) contact.get("visibility");
        if (contact.get("birthDate") == null) {
            birthDateTextField.setVisible(false);
        } else if (!contact.get("birthDate").toString().equals("Jan 1, 1970")) {
            birthDateTextField.setText(contact.get("birthDate").toString());
        } else {
            birthDateTextField.setVisible(false);
        }
        viewLinkHyperLink.setText((String) contact.get("viewLink"));
        fastConnectLink.setText((String) contact.get("fastConnect"));
        setVisibilityRadioButton(visibility);
    }

    private void fillProfilePosts() {

    }

    private HashMap<String, Object> fetchUserProfile() {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                String viewerEmail = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());
                if (viewerEmail.equals(profileEmail)) {
                    connectButton.setVisible(false);
                    followButton.setVisible(false);
                } else {
                    avatarImageView.setDisable(true);
                    backgroundButton.setVisible(false);
                    editButton.setVisible(false);
                    educationAddButton.setVisible(false);
                    contactUpdateButton.setVisible(false);
                    skillsUpdateButton.setVisible(false);
                    educationUpdateButton.setVisible(false);
                    onlyMeRadioButton.setVisible(false);
                    everyoneRadioButton.setVisible(false);
                    myConnectionRadioButton.setVisible(false);
                    addPostButton.setVisible(false);
                }

                URL url = new URL("http://localhost:8080/profile/" + profileEmail);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                HashMap<String, Object> profile = new HashMap<>();
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }

                        Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
                        profile = gson.fromJson(response.toString(), type);
                    }
                }
                return profile;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return null;
    }

    public static void setProfileEmail(String email) {
        ProfileController.profileEmail = email;
    }

    public static String getProfileEmail() {
        return ProfileController.profileEmail;
    }

//    public static void reloadProfile(Button btn) throws IOException {
//        Stage currentStage = (Stage) btn.getScene().getWindow();
//
//        Parent profilePage = FXMLLoader.load(ProfileController.class.getResource("/org/example/appclient/profile.fxml"));
//        Scene profilePageScene = new Scene(profilePage);
//        currentStage.setScene(profilePageScene);
//        currentStage.setFullScreen(true);
//    }

    private void uploadImage(String type) {
        File file = chooseFile();
        if (file != null) {
            try {
                String email = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());
                if (type.equals("avatar")) {
                    MediaController.uploadFile(file, "/upload-avatar", email);
                    avatarImageView.setImage(new Image(file.toURI().toString()));
                } else if (type.equals("background")) {
                    MediaController.uploadFile(file, "/upload-background", email);
                    backgroundImageView.setImage(new Image(file.toURI().toString()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        return fileChooser.showOpenDialog(null);
    }
}
