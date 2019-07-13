package ru.vlsv.client;

import ru.vlsv.common.AbstractMessage;
import ru.vlsv.common.FileMessage;
import ru.vlsv.common.FileRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> localFilesList;

    @FXML
    ListView<String> remoteFilesList;

    private static final String LOCAL_STORAGE = "client_storage";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get(LOCAL_STORAGE + "/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        String fileName = remoteFilesList.getSelectionModel().getSelectedItem();
//        if (tfFileName.getLength() > 0) {
//            Network.sendMsg(new FileRequest(tfFileName.getText()));
//            tfFileName.clear();
//        }
        Network.sendMsg(new FileRequest(fileName));
    }

    public void refreshLocalFilesList() {
        updateUI(() -> {
            try {
                localFilesList.getItems().clear();
                Files.list(Paths.get(LOCAL_STORAGE)).map(p -> p.getFileName().toString()).forEach(o -> localFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {
        String fileName = localFilesList.getSelectionModel().getSelectedItem();
        if (fileName != null) {
            Path path = Paths.get(LOCAL_STORAGE + "/" + fileName);
            System.out.println(path);
            try {
                Network.sendMsg(new FileMessage(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void pressOnLocalDeleteBtn(ActionEvent actionEvent) {
    }

    public void pressOnLocalRefreshBtn(ActionEvent actionEvent) {
        refreshLocalFilesList();
    }

    public void pressOnRemoteDeleteBtn(ActionEvent actionEvent) {
    }

    public void pressOnRemoteRefreshBtn(ActionEvent actionEvent) {
    }
}
