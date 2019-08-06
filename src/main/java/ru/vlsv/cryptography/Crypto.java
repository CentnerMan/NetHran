package ru.vlsv.Cryptography;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import ru.vlsv.client.LoginController;

import javax.crypto.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import static ru.vlsv.Cryptography.KeyStoreUtils.*;

/**
 * Java, NetHran.
 *
 * @author Anatoly Lebedev
 * @version 1.0.0 24.07.2019
 * @link https://github.com/Centnerman
 */

public class Crypto {

    private SecretKey originalKey = null;
    private Cipher cipher = null;
    private byte[] ivBytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};


    private void cryptoInitialization() {
        // Генерация и сохранение пароля для текущего пользователя
        Security.addProvider(new BouncyCastleProvider());
        String keyFile = LoginController.currentUser + ".key";
        File file = new File(keyFile);

        if (!Files.exists(Paths.get(keyFile))) {
            try {
                //Generating a key:
                originalKey = generateKey();
                //Saving a key:
                saveKey(originalKey, file);
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                //Loading a key:
                originalKey = loadKey(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    private void enCryptedFile(String fileName) {
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            if (cipher != null) {
                cipher.init(Cipher.ENCRYPT_MODE, originalKey);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

        if (fileName != null) {
            Path path = Paths.get(fileName);

            byte[] buff = new byte[1024]; // Tune if necessary
            int bytesRead = 0;
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(String.valueOf(path)))) {
                try (BufferedOutputStream bos = new BufferedOutputStream(new CipherOutputStream(new FileOutputStream(String.valueOf(path) + ".crypted"), cipher))) {
                    while ((bytesRead = bis.read(buff)) != -1) {
                        bos.write(buff, 0, bytesRead);
                    }
                    bos.flush();
                } // close bos
            } // close bis

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deCryptedFile(String fileName) {
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            if (cipher != null) {
                cipher.init(Cipher.DECRYPT_MODE, originalKey);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        if (fileName != null) {
            Path path = Paths.get(fileName);

            int bytesRead = 0;
            byte[] buff = new byte[1024]; // Tune if necessary
            try (BufferedInputStream bis = new BufferedInputStream(new CipherInputStream(new FileInputStream(String.valueOf(path)), cipher))) {
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(String.valueOf(path) + ".decripted"))) {
                    while ((bytesRead = bis.read(buff)) != -1) {
                        bos.write(buff, 0, bytesRead);
                    }
                    bos.flush();
                } // close bos
            } // close bis
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
