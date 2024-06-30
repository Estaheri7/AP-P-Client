package org.example.appclient.Controllers;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.appclient.util.JwtManager;

import java.util.HashMap;

public class PostCell extends ListCell<HashMap<String, String>> {

    private VBox content;
    private Label stageLabel;

    public PostCell() {
        super();
        content = new VBox();
        stageLabel = new Label();
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(HashMap<String, String> item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            content.getChildren().clear();
            HashMap<String, String> user = FetcherEmail.fetchProfileDetailsByEmail(item.get("author"));
            PostController.initPost(item, content, user.get("firstName") + " " + user.get("lastName"), user.get("avatar_url"), stageLabel);
            setGraphic(content);
        }
    }
}
