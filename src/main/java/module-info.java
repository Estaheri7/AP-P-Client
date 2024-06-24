module org.example.appclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens org.example.appclient to javafx.fxml;
    exports org.example.appclient;
    exports org.example.appclient.Controllers;
    opens org.example.appclient.Controllers to javafx.fxml;
}