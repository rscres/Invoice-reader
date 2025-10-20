package org.jb.invoicereader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jb.invoicereader.Database.UpdateDB;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 400);
        stage.setTitle("Lançador de despesas - Conexos");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        try {
            ConexosAPI conexosAPI = ConexosAPI.getInstance();
            new UpdateDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
        launch();
    }
}