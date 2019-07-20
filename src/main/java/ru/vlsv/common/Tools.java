package ru.vlsv.common;

import java.io.File;

public class Tools {

    public static final int MAX_OBJECT_SIZE = 50 * 1024 * 1024;
    public static final int MAX_FILE_SIZE = 49 * 1024 * 1024;
    public static final int PORT = 8190;


    public static boolean createDirIfNotExist(String currentPath) {
        boolean result = false;
        File userDir = new File(currentPath);
        if (!userDir.exists()) {
            if (userDir.mkdir()) {
                System.out.println("Папка " + currentPath + " создана");
                result = true;
            } else {
                System.out.println("Ошибка создания папки " + currentPath);
                result = false;
            }
        }
        return result;
    }
}
