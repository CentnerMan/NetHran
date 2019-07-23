package ru.vlsv.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ProgressController implements Initializable {

    @FXML
    ProgressBar progressBar;

    @FXML
    Label infoLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressBar.setProgress(0f);
        String COPYING = "Идет процесс копирования...";
        infoLabel.setText(COPYING);
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void close() {
        if (Platform.isFxApplicationThread()) ((Stage) infoLabel.getScene().getWindow()).close();
        else Platform.runLater(() -> ((Stage) infoLabel.getScene().getWindow()).close());
    }

    public static ProgressController showProgressStage(Class parentControllerClass) {
        Parent root;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            root = fxmlLoader.load(parentControllerClass.getResource("/progress.fxml").openStream());
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setResizable(false);
            stage.alwaysOnTopProperty();
            stage.show();
            return fxmlLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
