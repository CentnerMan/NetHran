package ru.vlsv.common;

import java.util.ArrayList;

/**
 * Java, NetHran.
 *
 * @author Anatoly Lebedev
 * @version 1.0.0 13.07.2019
 * @link https://github.com/Centnerman
 */

public class ListFilesMessage extends AbstractMessage {

    private static final long serialVersionUID = -1822410006238198933L;
    private ArrayList<String> fileList;

    public ArrayList<String> getFileList() {
        return fileList;
    }

    public ListFilesMessage(ArrayList<String> fileList) {
        this.fileList = fileList;
    }
}
