package ru.vlsv.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import ru.vlsv.common.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.vlsv.common.Tools.MAX_FILE_SIZE;
import static ru.vlsv.common.Tools.createDirIfNotExist;

public class MainController implements Initializable {

    @FXML
    ListView<String> localFilesList;

    @FXML
    ListView<String> remoteFilesList;

    private static final String LOCAL_STORAGE = "client_storage";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Thread t = new Thread(() -> {
            try {
                createDirIfNotExist(LOCAL_STORAGE);
                while (true) {
                    AbstractMessage am = Network.readObject();

                    if (am instanceof FileMessage) {

                        FileMessage fm = (FileMessage) am;
                        boolean lastPart = receiveFile(fm);
                        if (lastPart) {
                            refreshLocalFilesList();
                        }

                    } else if (am instanceof ListFilesMessage) {
                        ListFilesMessage lfm = (ListFilesMessage) am;
                        ArrayList<String> remoteFiles = lfm.getFileList();
                        updateUI(() -> {
                            remoteFilesList.getItems().clear();
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

    private void refreshLocalFilesList() {
        updateUI(() -> {
            try {
                localFilesList.getItems().clear();
                Files.list(Paths.get(LOCAL_STORAGE)).map(p -> p.getFileName().toString()).forEach(o -> localFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void refreshRemoteFileList() {
        Network.sendMsg(new ListFilesRequest());
    }

    private static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {
        sendFile();
    }

    public void pressOnLocalDeleteBtn(ActionEvent actionEvent) {
        String fileName = localFilesList.getSelectionModel().getSelectedItem();
        if (fileName != null) {
            Path pathToDelete = Paths.get(LOCAL_STORAGE + "/" + fileName);
            try {
                Files.delete(pathToDelete);
            } catch (IOException e) {
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

    public void sendFile() {

        String fileName = localFilesList.getSelectionModel().getSelectedItem();

        if (fileName != null) {
            Path path = Paths.get(LOCAL_STORAGE + "/" + fileName);
            try { // Делим файл на части и отправляем
                int numParts = (int) Math.ceil((double) Files.size(path) / MAX_FILE_SIZE); // Определяем количество частей
                // Данные части файла
                byte[] data = new byte[Math.min((int) Files.size(path), MAX_FILE_SIZE)]; // Если файл в один кусок - массив размером с файл
                RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");
                for (int i = 0; i < numParts; i++) {
                    raf.seek((long) i * MAX_FILE_SIZE);
                    int bytesRead = raf.read(data);
                    if (i == numParts - 1) { // обрезаем последний кусок по фактическому размеру
                        byte[] realData = new byte[bytesRead];
                        System.arraycopy(data, 0, realData, 0, bytesRead);
                        Network.sendMsg(new FileMessage(path, realData, i, numParts));
                    } else {
                        Network.sendMsg(new FileMessage(path, data, i, numParts));
                    }
                }
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean receiveFile(FileMessage fm) {
        if (fm.getFileName() == null || fm.getData() == null) {
            return false;
        } else {
            Path path = Paths.get(LOCAL_STORAGE + "/" + fm.getFileName());
            try {
                if (!Files.exists(path)) {
                    Files.createFile(path);
                }
                RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");
                raf.seek((long) fm.getPartNum() * MAX_FILE_SIZE);
                raf.write(fm.getData());
                raf.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (fm.getPartNum() == fm.getPartCount() - 1);
    }

}
