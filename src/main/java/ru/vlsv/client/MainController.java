package ru.vlsv.client;

import ru.vlsv.common.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable {

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
                    } else if (am instanceof ListFilesMessage) {
                        // Получили список файлов
                        ListFilesMessage lfm = (ListFilesMessage) am;
                        ArrayList<String> remoteFiles = lfm.getFileList();
                        updateUI(() -> {
                            remoteFilesList.getItems().clear();
//                                for (int i = 0; i < remoteFiles.size(); i++) {
//                                    remoteFilesList.getItems().add(remoteFiles.get(i));
//                                }
                            remoteFiles.forEach(o -> remoteFilesList.getItems().add(o));
                        });
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
        refreshRemoteFileList();
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        String fileName = remoteFilesList.getSelectionModel().getSelectedItem();
        Network.sendMsg(new FileRequest(fileName));
    }

    public void refreshLocalFilesList() {
        updateUI(() -> {
            try {
                localFilesList.getItems().clear();
                Files.list(Paths.get(LOCAL_STORAGE)).map(p -> p.getFileName().toString()).forEach(o -> localFilesList.getItems().add(o));
//                localFilesList.getItems().addAll(String.valueOf(Files.list(Paths.get(LOCAL_STORAGE)).collect(Collectors.toList())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void refreshRemoteFileList() {
        Network.sendMsg(new ListFilesRequest());
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
//            System.out.println(path);
            try {
                Network.sendMsg(new FileMessage(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void pressOnLocalDeleteBtn(ActionEvent actionEvent) {
        String fileName = localFilesList.getSelectionModel().getSelectedItem();
        if (fileName != null) {
            Path pathToDelete = Paths.get(LOCAL_STORAGE + "/" + fileName);
            try {
                Files.delete(pathToDelete);
//                System.out.println("Удален файл " + fileName);
            } catch (IOException e) {
//                System.out.println("Что-то пошло не так :(");
                e.printStackTrace();
            }
        }
        refreshLocalFilesList();
    }

    public void pressOnLocalRefreshBtn(ActionEvent actionEvent) {
        refreshLocalFilesList();
    }

    public void pressOnRemoteDeleteBtn(ActionEvent actionEvent) {
        String fileName = remoteFilesList.getSelectionModel().getSelectedItem();
        Network.sendMsg(new DeleteFileRequest(fileName));
    }

    public void pressOnRemoteRefreshBtn(ActionEvent actionEvent) {
        Network.sendMsg(new ListFilesRequest());
    }
}
