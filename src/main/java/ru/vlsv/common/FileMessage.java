package ru.vlsv.common;

import java.nio.file.Path;

/**
 * Java, NetHran.
 *
 * @author Anatoly Lebedev
 * @version 1.0.0 20.07.2019
 * @link https://github.com/Centnerman
 */

public class FileMessage extends AbstractMessage {
    private static final long serialVersionUID = -2177803426247643842L;
    private String fileName;
    private byte[] data;
    private int partNum;
    private int partCount;

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public int getPartNum() {
        return partNum;
    }

    public int getPartCount() {
        return partCount;
    }

    public FileMessage(Path path, byte[] data, int partNum, int partCount) {
        this.fileName = path.getFileName().toString();
        this.data = data;
        this.partNum = partNum;
        this.partCount = partCount;
    }
}
