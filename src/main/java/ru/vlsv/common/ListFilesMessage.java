package ru.vlsv.common;

import javafx.scene.control.ListView;

/**
 * Java, NetHran.
 *
 * @author Anatoly Lebedev
 * @version 1.0.0 13.07.2019
 * @link https://github.com/Centnerman
 */

public class ListFilesMessage extends AbstractMessage {

    private String[] fileList;

    public String[] getFileList() {
        return fileList;
    }

    public ListFilesMessage(String[] fileList) {
        this.fileList = fileList;
    }
}
