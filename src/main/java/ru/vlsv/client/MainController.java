package ru.vlsv.client;

import ru.vlsv.common.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static ru.vlsv.common.Tools.*;

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
                        Files.write(Paths.get(LOCAL_STORAGE + "/" + fm.getFileName()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();

                    } else if (am instanceof BigFileMessage) {
                        BigFileMessage bfm = (BigFileMessage) am;
                        boolean lastPart = receiveBigFile(bfm);
                        if (lastPart) refreshLocalFilesList();

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
//        String fileName = localFilesList.getSelectionModel().getSelectedItem();
//        if (fileName != null) {
//            Path path = Paths.get(LOCAL_STORAGE + "/" + fileName);
//            try {
//                Network.sendMsg(new FileMessage(path));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        sendBigFile();
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

    public void sendBigFile() {
        String fileName = localFilesList.getSelectionModel().getSelectedItem();
        System.out.println("Отправляем файл " + fileName);
        if (fileName != null) {
            Path path = Paths.get(LOCAL_STORAGE + "/" + fileName);
            try {
                // Делим файл на части и отправляем
                // Определяем количество частей
                int numParts = (int) Math.ceil((double) Files.size(path) / MAX_FILE_SIZE);
                System.out.println("Количество частей: " + numParts);
                // Данные части файла
                byte[] data;
                if (numParts == 1) {
                    data = new byte[(int) Files.size(path)];
                } else {
                    data = new byte[MAX_FILE_SIZE];
                }
                RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");

                for (int i = 0; i < numParts; i++) {
                    raf.seek((long) i * MAX_FILE_SIZE);
                    int bytesRead = raf.read(data);
                    byte[] realData = new byte[bytesRead];
                    System.arraycopy(data, 0, realData, 0, bytesRead); // TODO Как то победить последний кусок
                    Network.sendMsg(new BigFileMessage(path, realData, i, numParts));
                    System.out.println("Отправили часть: " + (i + 1) + " из " + numParts + " - " + bytesRead);
                }
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean receiveBigFile(BigFileMessage bfm) {
        if (bfm.getFileName() == null || bfm.getData() == null) {
            return false;
        } else {
            Path path = Paths.get(LOCAL_STORAGE + "/" + bfm.getFileName());
            try {
                if (!Files.exists(path)) {
                    Files.createFile(path);
                }
                RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");
                raf.seek((long) bfm.getPartNum() * MAX_FILE_SIZE);
                raf.write(bfm.getData());
                raf.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (bfm.getPartNum() == bfm.getPartCount() - 1);
    }
}
