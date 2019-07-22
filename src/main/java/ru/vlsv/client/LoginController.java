package ru.vlsv.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ru.vlsv.common.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Java, NetHran.
 *
 * @author Anatoly Lebedev
 * @version 1.0.0 14.07.2019
 * @link https://github.com/Centnerman
 */

public class LoginController implements Initializable {

    @FXML
    AnchorPane rootNode;

    @FXML
    TextField userLogin;

    @FXML
    PasswordField userPassword;

    public static String currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof AuthorizationOK) {
                        // Авторизация удалась - переключаем сцену
                        System.out.println("Авторизация удалась");
                        currentUser = userLogin.getText();
                        changeSceneToMain();
                        break;
                    } else if (am instanceof AuthorizationFalse) {
                        // Ошибка авторизации - очищаем поля ввода.
                        System.out.println("Ошибка авторизации");
                        userLogin.clear();
                        userPassword.clear();
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void pressLoginBtn(ActionEvent actionEvent) {
        Network.sendMsg(new AuthorizationRequest(userLogin.getText(), userPassword.getText()));
    }

    private void changeSceneToMain() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        Parent mainScene = FXMLLoader.load(getClass().getResource("/main.fxml"));
                        ((Stage) rootNode.getScene().getWindow()).setScene(new Scene(mainScene));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
