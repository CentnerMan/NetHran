package ru.vlsv.common;

public class FileRequest extends AbstractMessage {
    private static final long serialVersionUID = -744518978088093333L;
    private String filename;

    public String getFilename() {
        return filename;
    }

    public FileRequest(String filename) {
        this.filename = filename;
    }
}
