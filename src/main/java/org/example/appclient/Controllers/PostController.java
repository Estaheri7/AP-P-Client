package org.example.appclient.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.appclient.util.JwtManager;

import javax.swing.*;
import java.awt.image.BufferedImage;
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

import static org.example.appclient.Controllers.FetcherEmail.fetchProfileDetailsByEmail;
import static org.example.appclient.Controllers.FetcherEmail.goToProfile;

public class PostController {
    private static final Gson gson = new Gson();

    private static final HashSet<String> nonImageMedia = new HashSet<>();

    private static final HashSet<String> imageMedia = new HashSet<>();

    private static final ObservableList<HashMap<String, String>> allPosts = FXCollections.observableArrayList();


    public static void initializePosts(String user) {
        ArrayList<HashMap<String, String>> posts = fetchPostFromUser(user);
        allPosts.setAll(posts);
    }

    public static ObservableList<HashMap<String, String>> getAllPosts() {
        return allPosts;
    }

    public static ArrayList<HashMap<String, String>> fetchPostFromUser(String user) {
        ArrayList<HashMap<String, String>> posts = new ArrayList<>();

        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/posts/" + user);

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

    private static boolean isDialogOpen = false;

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
        postContentArea.setStyle("-fx-control-inner-background: #000000;");

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
        Node media = handleMedia("/post/media/", mediaUrl, postId);

        // labels
        HBox labelBox = new HBox(20);
        labelBox.setPadding(new Insets(0, 0, 0, 10));
        Label likeLabel = new Label(likes + " likes");
        Label commentLabel = new Label(comments + " comments");
        likeLabel.setStyle("-fx-font-weight: bold;" +
                "-fx-text-fill: #1d7754");
        commentLabel.setStyle("-fx-font-weight: bold;" +
                "-fx-text-fill: #1d7754");

        // buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setStyle("-fx-border-color: black");
        buttonBox.setPadding(new Insets(10));

        // like button
        Button likeButton = new Button("Like");
        if (isLiked(postId)) {
            likeButton.setText("Dislike");
        }
        String viewerEmail = (String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken());
        likeButton.setOnAction(event -> {
            if (!email.equals(viewerEmail)) {
                onLikeButton(postId, likeButton, likeLabel);
            }
        });

        // comment button
        Button commentButton = new Button("Comment");
//        likeButton.setTranslateX(100);
//        commentButton.setTranslateX(100);
        likeButton.setPrefWidth(70);
        commentButton.setPrefWidth(70);

        commentButton.setOnAction(event -> {
            CommentController.setPostId(postId);
            displayAddComment(commentLabel);
        });

        Button sendButton = new Button("Send");
        sendButton.setPrefWidth(70);
        Button repostButton = new Button("Repost");
        repostButton.setPrefWidth(70);
        sendButton.setTranslateX(60);
        repostButton.setTranslateX(60);

        // handling likes display
        likeLabel.setOnMouseClicked(event -> {
            ArrayList<HashMap<String, String>> likeEmails = fetchLikesForPost(postId);
            LikeController.setLikeEmails(likeEmails);
            LikeController.setTempLabel(likeLabel);
            displayLikeDialog(likeLabel);
        });

        labelBox.getChildren().addAll(likeLabel, commentLabel);
        buttonBox.getChildren().addAll(likeButton, commentButton, repostButton, sendButton);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        postVBox.setPadding(new Insets(10));
        postVBox.setSpacing(10);
        postVBox.getChildren().addAll(header, titleLabel, postContentArea, media, labelBox, buttonBox);

        postVBox.setOnMouseClicked(event -> {
            if (!isDialogOpen) {
                displayPostDialog(post, name, userAvatar, stageLabel, postVBox.getScene().getWindow());
            }
        });
    }

    private static void displayPostDialog(HashMap<String, String> post, String name, String userAvatar, Label stageLabel, Window owner) {
        isDialogOpen = true;

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Post Details");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));
        dialogVBox.setPrefSize(450, 600);

        initPost(post, dialogVBox, name, userAvatar, stageLabel);

        dialog.getDialogPane().setContent(dialogVBox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.setOnHidden(event -> isDialogOpen = false);
        dialog.showAndWait();
    }

    public static void onLikeButton(String postId, Button likeButton, Label likeLabel) {
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

    public static ArrayList<HashMap<String, String>> fetchLikesForPost(String postId) {
        ArrayList<HashMap<String, String>> likes = new ArrayList<>();
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://localhost:8080/posts/likes/" + postId);
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

                    Type type = new TypeToken<ArrayList<HashMap<String, String>>>() {}.getType();
                    likes = gson.fromJson(response.toString(), type);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return likes;
    }

    public static ArrayList<HashMap<String, String>> fetchCommentsForPost(String postId) {
        ArrayList<HashMap<String, String>> comments = new ArrayList<>();
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/posts/comments/" + postId);
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

                        Type type = new TypeToken<ArrayList<HashMap<String, String>>>() {}.getType();
                        comments = gson.fromJson(response.toString(), type);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return comments;
    }

    public static void displayAddComment(Label commentLabel) {
        try {
            CommentController.setTempLabel(commentLabel);

            FXMLLoader loader = new FXMLLoader(PostController.class.getResource("/org/example/appclient/addComment.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Write comment");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(commentLabel.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setResizable(false);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void displayLikeDialog(Label likeLabel) {
        try {
            FXMLLoader loader = new FXMLLoader(PostController.class.getResource("/org/example/appclient/likeDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Reactions");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(likeLabel.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setResizable(false);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
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

    public static boolean isLiked(String postId) {
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

    public static Node handleMedia(String requestURL, String mediaUrl, String postId) {
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            return new ImageView();
        }

        fillNonImageMedia();
        fillImageMedia();
        Node mediaNode = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://localhost:8080" + requestURL + postId);
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
                } else if ("pdf".equalsIgnoreCase(fileExtension)) {
                    PDDocument document = PDDocument.load(connection.getInputStream());
                    PDFRenderer pdfRenderer = new PDFRenderer(document);
                    VBox vbox = new VBox();

                    ImageView imageView = new ImageView();
                    imageView.setFitWidth(425);
                    imageView.setPreserveRatio(true);
                    vbox.getChildren().add(imageView);

                    HBox controls = new HBox();
                    Button prevButton = new Button("Previous");
                    Button nextButton = new Button("Next");
                    controls.getChildren().addAll(prevButton, nextButton);
                    vbox.getChildren().add(controls);

                    final int[] currentPage = {0};
                    final int totalPages = document.getNumberOfPages();

                    prevButton.setOnAction(event -> {
                        if (currentPage[0] > 0) {
                            currentPage[0]--;
                            updatePdfPage(pdfRenderer, imageView, currentPage[0]);
                        }
                    });

                    nextButton.setOnAction(event -> {
                        if (currentPage[0] < totalPages - 1) {
                            currentPage[0]++;
                            updatePdfPage(pdfRenderer, imageView, currentPage[0]);
                        }
                    });

                    updatePdfPage(pdfRenderer, imageView, currentPage[0]);
                    mediaNode = new ScrollPane(vbox);
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

    private static void updatePdfPage(PDFRenderer pdfRenderer, ImageView imageView, int pageIndex) {
        try {
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, 300);
            WritableImage fxImage = convertToWritableImage(bufferedImage);
            imageView.setImage(fxImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static WritableImage convertToWritableImage(BufferedImage bufferedImage) {
        WritableImage writableImage = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int argb = bufferedImage.getRGB(x, y);
                pixelWriter.setArgb(x, y, argb);
            }
        }
        return writableImage;
    }

    public static ImageView handleAvatar(String email, String userAvatar, Label stageLabel) {
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
