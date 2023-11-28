module com.tictacto.tictacto {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.tictacto.tictacto to javafx.fxml;
    exports com.tictacto.tictacto;
}