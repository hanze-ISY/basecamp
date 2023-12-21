module com.tictacto.tictacto {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.tictacto.tictacto to javafx.fxml;
    exports com.tictacto.tictacto;
}