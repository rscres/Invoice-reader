package org.jb.invoicereader;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;

import java.io.File;

public class GuiController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            db.getFiles().forEach(file -> {
                System.out.println("Received file: " + file.getAbsolutePath());
                welcomeText.setText("Dropped: " + file.getName());
            });
            success = true;
        }

        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    void handleFileOverEvent(DragEvent event)
    {
        Dragboard db = event.getDragboard();
        if (db.hasFiles())
        {
            event.acceptTransferModes(TransferMode.COPY);
        }
        else
        {
            event.consume();
        }
    }

    @FXML
    void onDragEntered(DragEvent event) {
        VBox vbox = (VBox) event.getSource();
        vbox.setStyle("-fx-border-color: blue; -fx-background-color: #e6f3ff;");
    }

    @FXML
    void onDragExited(DragEvent event) {
        VBox vbox = (VBox) event.getSource();
        vbox.setStyle("");
    }
}