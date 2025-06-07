module com.example.lestour {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.web;
    requires jdk.jsobject;



    opens com.example.lestour to javafx.fxml;
    exports com.example.lestour;
}