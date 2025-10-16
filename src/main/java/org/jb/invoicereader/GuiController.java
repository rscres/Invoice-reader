package org.jb.invoicereader;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import org.jb.invoicereader.DataHandlers.DataExtractor;

import java.io.File;

public class GuiController {
    @FXML
    private Label welcomeText;
    @FXML
    private Button createButton;
    @FXML
    private HBox fileInput;
    private DataExtractor extractor;

    @FXML
    protected void onCreateButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onExtractButtonClick() {
        createButton.setDisable(false);
        TextField text = (TextField) fileInput.getChildren().getFirst();
        extractor = new DataExtractor(text.getText());
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