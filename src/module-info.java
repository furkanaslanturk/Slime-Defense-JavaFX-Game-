module JavaFx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
	requires javafx.graphics;

    opens application to javafx.fxml;
    exports application;
}
