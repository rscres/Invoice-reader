module org.jb.invoicereader {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires tools.jackson.databind;
    requires java.net.http;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens org.jb.invoicereader to javafx.fxml;
    exports org.jb.invoicereader;
    exports org.jb.invoicereader.DataHandlers;
    opens org.jb.invoicereader.DataHandlers to javafx.fxml;
    exports org.jb.invoicereader.Database;
    opens org.jb.invoicereader.Database to javafx.fxml;
}