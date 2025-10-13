module org.jb.invoicereader {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens org.jb.invoicereader to javafx.fxml;
    exports org.jb.invoicereader;
}