package org.example.appclient.Controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.example.appclient.util.JwtManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MessagePageController {

    private final Gson gson = new Gson();

    @FXML
    private ScrollPane userScrollPane;

    private VBox userVbox;

    @FXML
    private Label usersLabel;

    public void initialize() {
        userVbox = new VBox();
        userScrollPane.setContent(userVbox);
        displayMessages();
    }

    public void displayMessages() {
        ArrayList<String> senders = FetcherEmail.fetchEmails("user/chat", "sender",(String) JwtManager.decodeJwtPayload(JwtManager.getJwtToken()));
        HashSet<String> senderSet = new HashSet<>(senders);
        userVbox.getChildren().clear();
        for (String senderEmail : senderSet) {
            HashMap<String, String> userDetail = FetcherEmail.fetchProfileDetailsByEmail(senderEmail);
            FetcherEmail.addEntry(userDetail, userVbox, usersLabel);
        }
    }
}
