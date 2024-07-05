package org.example.appclient.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.appclient.LinkedInApp;
import org.example.appclient.util.JwtManager;

import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatController {

    private static String receiver;

    private HashMap<String, String> user;

    private ArrayList<HashMap<String, String>> chats;

    private File mediaFile;

    private final Gson gson = new Gson();

    @FXML
    private ImageView avatarImageView;

    @FXML
    private Label headlineLabel;

    @FXML
    private ListView<HashMap<String, String>> mainContainer;

    @FXML
    private Button mediaButton;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private Label nameLabel;

    @FXML
    private Button sendButton;

    private Node mediaNode;

    @FXML
    void onMediaButton(ActionEvent event) {
        openFileChooser();
        if (mediaFile != null) {
            try {
                MediaController.uploadFile(mediaFile, "/send-file", receiver);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void onSendButton(ActionEvent event) {
        new Thread(this::sendMessage).start();
    }

    private void sendMessage() {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                String message = messageTextArea.getText();

                if (message.isEmpty()) {
                    messageTextArea.setText("Write something first!");
                    messageTextArea.selectAll();
                    return;
                }

                URL url = new URL("http://localhost:8080/send-message/" + receiver);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                HashMap<String, String> chatData = new HashMap<>();
                chatData.put("message", message);

                String jsonData = gson.toJson(chatData);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonData.getBytes());
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Platform.runLater(() -> {
                        messageTextArea.clear();
                        messageTextArea.setFocusTraversable(true);
                        addMessageToChat(message, JwtManager.decodeJwtPayload(JwtManager.getJwtToken()).toString());
                        scrollToBottom();
                    });
                } else {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                        System.out.println(br.readLine());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addMessageToChat(String message, String sender) {
        HashMap<String, String> chatData = new HashMap<>();
        chatData.put("sender", sender);
        chatData.put("message", message);
        chats.add(chatData);
        mainContainer.getItems().add(chatData);
    }

    public static void openChat(String receiver, Button btn) {
        try {
            ChatController.receiver = receiver;

            Parent profilePage = FXMLLoader.load(ChatController.class.getResource("/org/example/appclient/privateChat.fxml"));
            Stage currentStage = (Stage) btn.getScene().getWindow();
            Scene scene = new Scene(profilePage);

            Stage profileStage = new Stage();
            profileStage.setScene(scene);
            LinkedInApp.setIcon(profileStage);
            profileStage.setTitle("Chat");
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

    private void fetchReceiverProfile(Runnable callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/user/" + receiver);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = br.readLine()) != null) {
                            response.append(inputLine);
                        }

                        Type type = new TypeToken<HashMap<String, String>>(){}.getType();
                        user = gson.fromJson(response.toString(), type);
                        Platform.runLater(callback);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void fillTop() {
        fetchReceiverProfile(() -> {
            String name = user.get("firstName") + " " + user.get("lastName");
            String headline = user.get("headline");
            String avatar = user.get("avatar_url");

            nameLabel.setText(name);
            headlineLabel.setText(headline);

            if (avatar != null && !avatar.isEmpty()) {
                fetchAvatarImage();
            } else {
                Image image = new Image(getClass().getResource("/org/example/appclient/images/defaultAvatar.png").toExternalForm());
                avatarImageView.setImage(image);
            }

            avatarImageView.setOnMouseClicked(event -> FetcherEmail.goToProfile(receiver, nameLabel));
        });
    }

    private void fetchAvatarImage() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/user/avatar/" + receiver);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    Image image = new Image(inputStream);
                    Platform.runLater(() -> avatarImageView.setImage(image));
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchChats(Runnable callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/chats/" + receiver);
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

                        Type type = new TypeToken<ArrayList<HashMap<String, String>>>(){}.getType();
                        chats = gson.fromJson(response.toString(), type);
                        Platform.runLater(callback);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void fillChat() {
        fetchChats(() -> {
            mainContainer.getItems().setAll(chats);
            scrollToBottom();
        });
    }

    private void pollForNewMessages() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            while (true) {
                try {
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL("http://localhost:8080/chats/" + receiver);
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

                                Type type = new TypeToken<ArrayList<HashMap<String, String>>>(){}.getType();
                                ArrayList<HashMap<String, String>> newChats = gson.fromJson(response.toString(), type);

                                Platform.runLater(() -> updateChat(newChats));
                            }
                        }
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private void updateChat(ArrayList<HashMap<String, String>> newChats) {
        if (newChats.size() != chats.size()) {
            mainContainer.getItems().setAll(newChats);
            chats = newChats;
            scrollToBottom();
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> mainContainer.scrollTo(chats.size() - 1));
    }

    public void initialize() {
        messageTextArea.setFocusTraversable(false);
        messageTextArea.setPromptText("Write a message...");

        Platform.runLater(() -> {
            messageTextArea.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    onSendButton(new ActionEvent());
                }
            });
        });

        Circle circle = new Circle(35,35,35);
        avatarImageView.setClip(circle);

        mainContainer.getStylesheets().add(getClass().getResource("/org/example/appclient/css/listview.css").toExternalForm());

        mainContainer.setCellFactory(new Callback<>() {
            @Override
            public ListCell<HashMap<String, String>> call(ListView<HashMap<String, String>> listView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(HashMap<String, String> chat, boolean empty) {
                        super.updateItem(chat, empty);
                        if (empty || chat == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            String sender = chat.get("sender");
                            String message = chat.get("message");
                            String time = chat.get("timestamp");

                            HBox messageContainer = new HBox();
                            messageContainer.setSpacing(3);
                            Label messageLabel = new Label(message);
                            Label timeLabel = new Label(time);
                            timeLabel.setStyle("-fx-text-fill: white");
                            messageLabel.setWrapText(true);
                            messageLabel.setPadding(new Insets(10));
                            messageLabel.setStyle("-fx-background-color: lightgray; -fx-background-radius: 10;");

                            if (isSender(sender)) {
                                messageContainer.setAlignment(Pos.BOTTOM_RIGHT);
                                messageLabel.setStyle("-fx-background-color: lightgreen; -fx-background-radius: 10;");
                            } else {
                                messageContainer.setAlignment(Pos.BOTTOM_LEFT);
                            }

                            if (message.startsWith("chat_media/")) {
                                String mediaURL = message.substring("chat_media/".length()).split("\\.")[0];
                                mediaNode = PostController.handleMedia("/chat-media/", message, mediaURL);
                                messageContainer.getChildren().add(mediaNode);
                            } else {
                                messageContainer.getChildren().add(messageLabel);
                                messageContainer.setPadding(new Insets(5));
                            }

                            if (isSender(sender)) {
                                messageContainer.getChildren().add(0, timeLabel);
                            } else {
                                messageContainer.getChildren().add(1, timeLabel);
                            }

                            setGraphic(messageContainer);
                        }
                    }
                };
            }
        });

        fillTop();
        fillChat();
        pollForNewMessages();
    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.pdf");
        FileChooser.ExtensionFilter audioFilter = new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac", "*.ogg");
        FileChooser.ExtensionFilter videoFilter = new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mkv", "*.avi");
        fileChooser.getExtensionFilters().addAll(imageFilter, audioFilter, videoFilter);
        mediaFile = fileChooser.showOpenDialog(sendButton.getScene().getWindow());
    }

    private boolean isSender(String sender) {
        return sender.equals(JwtManager.decodeJwtPayload(JwtManager.getJwtToken()));
    }
}
