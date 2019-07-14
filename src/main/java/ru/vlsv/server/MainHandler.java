package ru.vlsv.server;

import ru.vlsv.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private static final String SERVER_STORAGE = "server_storage";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FileRequest) {
                FileRequest fr = (FileRequest) msg;
                if (Files.exists(Paths.get(SERVER_STORAGE + "/" + fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get(SERVER_STORAGE + "/" + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }

            } else if (msg instanceof FileMessage) {
                // Что делать если прилетел файл ??
                FileMessage fm = (FileMessage) msg;
                Path pathToNewFile = Paths.get(SERVER_STORAGE + "/" + fm.getFilename());
                if (Files.exists(pathToNewFile)) {
                    System.out.println("Файл с именем " + fm.getFilename() + " уже существует");
                } else {
                    Files.write(Paths.get(SERVER_STORAGE + "/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                }
                RefreshServerFileList(ctx);

            } else if (msg instanceof ListFilesRequest) {
                RefreshServerFileList(ctx);

            } else if (msg instanceof DeleteFileRequest) {
                DeleteFileRequest dfr = (DeleteFileRequest) msg;
                Path pathToDelete = Paths.get(SERVER_STORAGE + "/" + dfr.getFilename());
                try {
                    Files.delete(pathToDelete);
//                    System.out.println("Удален файл " + dfr.getFilename());
                } catch (IOException e) {
//                    System.out.println("Что-то пошло не так :(");
                    e.printStackTrace();
                }
                RefreshServerFileList(ctx);
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void RefreshServerFileList(ChannelHandlerContext ctx) {
        ListFilesMessage lfm = new ListFilesMessage(CreateServerFileList());
        ctx.writeAndFlush(lfm);
    }

    private ArrayList<String> CreateServerFileList() {
        ArrayList<String> serverFiles = new ArrayList<>();
        File folder = new File(SERVER_STORAGE);
        File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            serverFiles.add(files[i].getName());
        }
        return serverFiles;
    }

}
