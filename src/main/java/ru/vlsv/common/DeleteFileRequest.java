package ru.vlsv.common;

/**
 * Java, NetHran.
 *
 * @author Anatoly Lebedev
 * @version 1.0.0 13.07.2019
 * @link https://github.com/Centnerman
 */

public class DeleteFileRequest extends AbstractMessage {
    private static final long serialVersionUID = -5748364821177024097L;
    private String filename;

    public String getFilename() {
        return filename;
    }

    public DeleteFileRequest(String filename) {
        this.filename = filename;
    }
}
