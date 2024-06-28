package org.example.appclient.Controllers;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.appclient.util.JwtManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class UserConnectionController {

    private final Gson gson = new Gson();

    @FXML
    private ScrollPane connectionsScrollPane;

    @FXML
    private TextField searchByName;

    @FXML
    private VBox usersVBox;

    @FXML
    private Label connectionLabel;

    public void displayConnections() {
        ArrayList<String> senderEmails = FetcherEmail.fetchEmails("connections", "sender", ProfileController.getProfileEmail());
        connectionLabel.setText(senderEmails.size() + " Connection");
        usersVBox.getChildren().clear();
        for (String senderEmail : senderEmails) {
            HashMap<String, String> userDetail = FetcherEmail.fetchProfileDetailsByEmail(senderEmail);
            FetcherEmail.addEntry(userDetail, usersVBox, connectionLabel);
        }
    }

    public void initialize() {
        usersVBox = new VBox();
        connectionsScrollPane.setContent(usersVBox);
        displayConnections();
    }
}

