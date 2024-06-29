package org.example.appclient.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
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
import java.util.HashSet;
import java.util.Set;

import static org.example.appclient.Controllers.FetcherEmail.goToProfile;

public class PostController {
    private static final Gson gson = new Gson();

    private static boolean hasLiked = false;

    private static final HashSet<String> nonImageMedia = new HashSet<>();

    private static final HashSet<String> imageMedia = new HashSet<>();

    public static ArrayList<HashMap<String, String>> fetchPostFromUser(String user, int page, int pageSize) {
        ArrayList<HashMap<String, String>> posts = new ArrayList<>();

        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/posts/" + user + "?page=" + page + "&size=" + pageSize);

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

                        Type type = new TypeToken<ArrayList<HashMap<String, String>>>() {}.getType();
                        posts = gson.fromJson(response.toString(), type);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return posts;
    }

    public static void initPost(HashMap<String, String> post, VBox postVBox, String name, String userAvatar, Label stageLabel) {
        String postId = post.get("id");
        String email = post.get("author");
        String title = post.get("title");
        String content = post.get("content");
        String mediaUrl = post.get("mediaUrl");
        String createdAt = post.get("createdAt");
        int likes = Integer.parseInt(post.get("likes"));
        int comments = Integer.parseInt(post.get("comments"));

        ImageView avatar = handleAvatar(email, userAvatar, stageLabel);
        Circle circle = new Circle(25, 25, 25);
        avatar.setClip(circle);
        avatar.setTranslateX(-10);

        VBox profileInfo = new VBox();

        Label nameLabel = new Label(name);
        nameLabel.setTranslateX(-10);
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label createdAtLabel = new Label(createdAt);
        createdAtLabel.setTranslateX(-7);

        HBox header = new HBox(10);
        header.setPadding(new Insets(10));
        header.getChildren().addAll(avatar, profileInfo);
        profileInfo.getChildren().addAll(nameLabel, createdAtLabel);

        TextArea postContentArea = new TextArea(content);
        postContentArea.setFont(Font.font("System", FontWeight.THIN, 12));
        postContentArea.setWrapText(true);
        postContentArea.setEditable(false);
        postContentArea.setPrefWidth(425);
        postContentArea.setPrefHeight(50);

        // see more
        postContentArea.setOnMouseClicked(event -> {
            int len = postContentArea.getText().length();
            if (postContentArea.getPrefHeight() == 50 && len > 70) {
                postContentArea.setPrefHeight(200);
            } else {
                postContentArea.setPrefHeight(50);
            }
        });

        // media handling
        Node media = handleMedia(mediaUrl, postId);

        HBox buttonBox = new HBox(20);
        buttonBox.setStyle("-fx-border-color: black");
        Label likeLabel = new Label(likes + " likes");
        Label commentLabel = new Label(comments + " comments");
        buttonBox.setPadding(new Insets(10));
        Button likeButton = new Button("Like");
        if (isLiked(postId)) {
            likeButton.setText("Dislike");
        }

        Button commentButton = new Button("Comments");
        likeButton.setTranslateX(100);
        commentButton.setTranslateX(100);
        likeButton.setPrefWidth(70);

        String viewerEmail = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());
        likeButton.setOnAction(event -> {
            if (!email.equals(viewerEmail)) {
                onLikeButton(postId, likeButton, likeLabel);
            }
        });

        buttonBox.getChildren().addAll(likeLabel, commentLabel, likeButton, commentButton);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        postVBox.setPadding(new Insets(10));
        postVBox.setSpacing(10);
        postVBox.getChildren().addAll(header, titleLabel, postContentArea, media, buttonBox);
    }

    private static void onLikeButton(String postId, Button likeButton, Label likeLabel) {
        if (JwtManager.isJwtTokenAvailable()) {
            if (likeButton.getText().equals("Like")) {
                like(postId, likeButton, likeLabel);
            } else {
                dislike(postId, likeButton, likeLabel);
            }
        }
    }

    private static void like(String postId, Button likeButton, Label likeLabel) {
        likeOrDislike("like", postId, likeButton, likeLabel);
    }

    private static void dislike(String postId, Button likeButton, Label likeLabel) {
        likeOrDislike("dislike", postId, likeButton, likeLabel);
    }

    private static void likeOrDislike(String action, String postId, Button likeButton, Label likeLabel) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/posts/" + action + "//" + postId);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    if (likeButton.getText().equals("Like")) {
                        increaseLike(likeLabel);
                        likeButton.setText("Dislike");
                    } else {
                        decreaseLike(likeLabel);
                        likeButton.setText("Like");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void increaseLike(Label likeLabel) {
        String currentLikes = likeLabel.getText().split(" ")[0];
        likeLabel.setText(Integer.valueOf(currentLikes) + 1 + " likes");
    }

    private static void decreaseLike(Label likeLabel) {
        String currentLikes = likeLabel.getText().split(" ")[0];
        likeLabel.setText(Integer.parseInt(currentLikes) - 1 + " likes");
    }

    private static boolean isLiked(String postId) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/posts/isLiked/" + postId);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private static void fillNonImageMedia() {
        // Video file extensions
        nonImageMedia.add("mp4");
        nonImageMedia.add("flv");
        nonImageMedia.add("avi");
        nonImageMedia.add("mov");

        // Audio file extensions
        nonImageMedia.add("mp3");
        nonImageMedia.add("wav");
        nonImageMedia.add("aac");
        nonImageMedia.add("ogg");

        // Additional formats
        nonImageMedia.add("webm");
        nonImageMedia.add("m4v");

    }

    private static void fillImageMedia() {
        imageMedia.add("jpg");
        imageMedia.add("jpeg");
        imageMedia.add("png");
        imageMedia.add("gif");
    }

    private static Node handleMedia(String mediaUrl, String postId) {
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            return new ImageView();
        }

        fillNonImageMedia();
        fillImageMedia();
        Node mediaNode = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://localhost:8080/post/media/" + postId);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            String fileExtension = mediaUrl.substring(mediaUrl.lastIndexOf(".") + 1);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if (imageMedia.contains(fileExtension)) {
                    Image image = new Image(connection.getInputStream());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(425);
                    imageView.setPreserveRatio(true);
                    mediaNode = imageView;
                } else if (nonImageMedia.contains(fileExtension)) {
                    Media media = new Media(url.toExternalForm());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    MediaView mediaView = new MediaView(mediaPlayer);
                    mediaView.setFitWidth(425);
                    mediaView.setPreserveRatio(true);
                    mediaPlayer.setAutoPlay(false);
                    mediaNode = mediaView;
                    mediaView.setOnMouseClicked(event -> {
                        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                            mediaPlayer.seek(Duration.ZERO);
                            mediaPlayer.play();
                        } else {
                            mediaPlayer.play();
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return mediaNode != null ? mediaNode : new ImageView();
    }

    private static ImageView handleAvatar(String email, String userAvatar, Label stageLabel) {
        ImageView avatar = new ImageView();
        avatar.setFitWidth(50);
        avatar.setFitHeight(50);
        if (userAvatar != null && !userAvatar.isEmpty()) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080/user/avatar/" + email).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    Image image = new Image(inputStream);
                    avatar.setImage(image);
                    avatar.setOnMouseClicked(event -> goToProfile(email, stageLabel));
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Image image = new Image(FetcherEmail.class.getResource("/org/example/appclient/images/linkedInIcon.png").toExternalForm());
            avatar.setImage(image);
            avatar.setOnMouseClicked(event -> goToProfile(email, stageLabel));
        }
        return avatar;
    }
}