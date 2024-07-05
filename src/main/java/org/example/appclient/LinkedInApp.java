package org.example.appclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class LinkedInApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1920, 1080);

//        stage.setResizable(false);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setScene(scene);
        stage.setTitle("LinkedIn App");
        setIcon(stage);
        stage.show();
    }

    public static void setIcon(Stage stage) {
        stage.getIcons().addAll(new Image(LinkedInApp.class.getResource("/org/example/appclient/images/linkedInIcon.png").toExternalForm()));
    }
}
