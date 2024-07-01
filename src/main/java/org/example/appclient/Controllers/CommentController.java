package org.example.appclient.Controllers;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.appclient.util.JwtManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class CommentController {

    @FXML
    private TextArea commentTextArea;

    @FXML
    private VBox mainContainer;

    @FXML
    private Button sendButton;

    private ListView<HashMap<String, String>> commentListView;

    private static String postId;

    private final Gson gson = new Gson();

    public static Label tempLabel;

    @FXML
    void onSendButton(ActionEvent event) {
        addComment();
    }

    private void addComment() {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/posts/add-comment/" + postId);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("AUTHORIZATION", "Bearer " + JwtManager.getJwtToken());
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String comment = commentTextArea.getText();
                if (comment == null || comment.isEmpty()) {
                    commentTextArea.setText("Write something first!!!");
                    return;
                }

                HashMap<String, String> commentData = new HashMap<>();
                commentData.put("comment", comment);

                String json = gson.toJson(commentData);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(json.getBytes());
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Stage stage = (Stage) sendButton.getScene().getWindow();
                    stage.close();
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

    private void displayComments() {
        Platform.runLater(() -> {
            ArrayList<HashMap<String, String>> commentDetails  = createCommentDetails();
            commentListView.getItems().clear();
            commentListView.getItems().addAll(commentDetails);
        });
    }

    private ArrayList<HashMap<String, String>> createCommentDetails() {
        ArrayList<HashMap<String, String>> commentDetails = new ArrayList<>();
        ArrayList<HashMap<String, String>> allComments = PostController.fetchCommentsForPost(postId);
        for (HashMap<String, String> comment : allComments) {
            String email = comment.get("email");
            String name = comment.get("userName");
            String commentDate = comment.get("commentDate");
            String message = comment.get("comment");
            HashMap<String, String> user = FetcherEmail.fetchProfileDetailsByEmail(email);
            String avatarURL = user.get("avatar_url");

            HashMap<String, String> commentData = new HashMap<>();
            commentData.put("email", email);
            commentData.put("name", name);
            commentData.put("commentDate", commentDate);
            commentData.put("message", message);
            commentData.put("avatarURL", avatarURL);
            commentDetails.add(commentData);
        }

        return commentDetails;
    }

    public void initialize() {
        commentListView = new ListView<>();
        commentListView.setStyle("-fx-background-color:  #232323;");
        commentListView.getStylesheets().add(getClass().getResource("/org/example/appclient/css/ScrollBar.css").toExternalForm());
        commentListView.setCellFactory(param -> new CommentCell());
        mainContainer.getChildren().add(commentListView);
        displayComments();
    }

    public static void setPostId(String postId) {
        CommentController.postId = postId;
    }

    public static void setTempLabel(Label temp) {
        CommentController.tempLabel = temp;
    }
}