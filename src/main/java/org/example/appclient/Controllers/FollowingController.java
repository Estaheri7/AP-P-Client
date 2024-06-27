package org.example.appclient.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;

public class FollowingController {

    @FXML
    private Label followingLabel;


    @FXML
    private VBox usersVBox;

    @FXML
    private ScrollPane followingScrollPane;

    public void displayFollowing() {
        ArrayList<String> followingEmail = FetcherEmail.fetchEmails("followings", "following");
        followingLabel.setText(followingEmail.size() + " Followings");
        usersVBox.getChildren().clear();
        for (String senderEmail : followingEmail) {
            HashMap<String, String> userDetail = FetcherEmail.fetchProfileDetailsByEmail(senderEmail);
            FetcherEmail.addEntry(userDetail, usersVBox, followingLabel);
        }
    }

    public void initialize() {
        usersVBox = new VBox();
        followingScrollPane.setContent(usersVBox);
        displayFollowing();
    }

}
