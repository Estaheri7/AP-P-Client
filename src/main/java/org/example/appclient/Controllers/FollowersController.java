package org.example.appclient.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;

public class FollowersController {

    @FXML
    private Label followerLabel;

    @FXML
    private VBox usersVBox;

    @FXML
    private ScrollPane followersScrollPane;

    public void displayFollowers() {
        ArrayList<String> followersEmail = FetcherEmail.fetchEmails("followers", "follower", ProfileController.getProfileEmail());
        followerLabel.setText(followersEmail.size() + " Followers");
        usersVBox.getChildren().clear();
        for (String senderEmail : followersEmail) {
            HashMap<String, String> userDetail = FetcherEmail.fetchProfileDetailsByEmail(senderEmail);
            FetcherEmail.addEntry(userDetail, usersVBox, followerLabel);
        }
    }

    public void initialize() {
        usersVBox = new VBox();
        followersScrollPane.setContent(usersVBox);
        displayFollowers();
    }

}
