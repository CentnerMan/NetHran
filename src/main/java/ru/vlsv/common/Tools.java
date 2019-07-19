package ru.vlsv.common;

import java.io.File;

public class Tools {

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
