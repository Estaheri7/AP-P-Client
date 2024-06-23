module org.example.appclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.appclient to javafx.fxml;
    exports org.example.appclient;
}