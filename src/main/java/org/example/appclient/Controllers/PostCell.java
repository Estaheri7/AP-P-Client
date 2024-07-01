package org.example.appclient.Controllers;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import java.util.HashMap;

public class PostCell extends ListCell<HashMap<String, String>> {

    private static Label tempLabel;

    private VBox content;

    public PostCell() {
        super();
        content = new VBox();
        content.setStyle("-fx-background-color: #1b1b1b");
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        // Set the background color of the cell
        setStyle("-fx-background-color: #1d7754");
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
            PostController.initPost(item, content, user.get("firstName") + " " + user.get("lastName"), user.get("avatar_url"), tempLabel);
            setGraphic(content);
        }
    }

    public static void setTempLabel(Label tempLabel) {
        PostCell.tempLabel = tempLabel;
    }
}
