package ru.vlsv.Cryptography;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.*;

public class EncriptionDecriptionExample {

    public void EncriptionDecription() throws IOException, NoSuchProviderException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        // BouncyCastle JCA provider
        Security.addProvider(new BouncyCastleProvider());

        // IV generation
        byte[] ivBytes = new byte[16]; // << should be generated cryptographically-secure and placed near file; regenerated on every file re-encryption
        new SecureRandom().nextBytes(ivBytes);

        // Key generation
        KeyGenerator key = KeyGenerator.getInstance("AES", "BC");
        key.init(128); // bits using SecureRandom
        SecretKey secretKey = key.generateKey();

        // Cipher init - Encryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));

        byte[] buff = new byte[1024]; // Tune if necessary
        int bytesRead = 0;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream("box.gif"))) {
            try (BufferedOutputStream bos = new BufferedOutputStream(new CipherOutputStream(new FileOutputStream("box.gif.crypted"), cipher))) {
                while ((bytesRead = bis.read(buff)) != -1) {
                    bos.write(buff, 0, bytesRead);
                }
                bos.flush();
            } // close bos
        } // close bis


        // Cipher init - Decryption
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));
        buff = new byte[1024]; // Tune if necessary
        try (BufferedInputStream bis = new BufferedInputStream(new CipherInputStream(new FileInputStream("box.gif.crypted"), cipher))) {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("box_unencrypted.gif"))) {
                while ((bytesRead = bis.read(buff)) != -1) {
                    bos.write(buff, 0, bytesRead);
                }
                bos.flush();
            } // close bos
        } // close bis
    }
}
