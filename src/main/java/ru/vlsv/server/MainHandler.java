package ru.vlsv.server;

import ru.vlsv.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private static final String SERVER_STORAGE = "server_storage";

    // тестируем процесс идентификации
    private static final String DEFAULT_LOGIN = "admin";
    private static final String DEFAULT_PASSWORD = "admin";


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof AuthorizationRequest) {
                AuthorizationRequest ar = (AuthorizationRequest) msg;
                if (ar.getName().equals(DEFAULT_LOGIN) || ar.getPassword().equals(DEFAULT_PASSWORD)) {
                    AuthorizationOK ok = new AuthorizationOK();
                    ctx.writeAndFlush(ok);
                } else {
                    AuthorizationFalse authFalse = new AuthorizationFalse();
                    ctx.writeAndFlush(authFalse);
                }

            } else if (msg instanceof FileRequest) {
                FileRequest fr = (FileRequest) msg;
                if (Files.exists(Paths.get(SERVER_STORAGE + "/" + fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get(SERVER_STORAGE + "/" + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }

            } else if (msg instanceof FileMessage) {
                FileMessage fm = (FileMessage) msg;
                Path pathToNewFile = Paths.get(SERVER_STORAGE + "/" + fm.getFilename());
                if (Files.exists(pathToNewFile)) {
                    System.out.println("Файл с именем " + fm.getFilename() + " уже существует");
                } else {
                    Files.write(Paths.get(SERVER_STORAGE + "/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                }
                refreshServerFileList(ctx);

            } else if (msg instanceof ListFilesRequest) {
                refreshServerFileList(ctx);

            } else if (msg instanceof DeleteFileRequest) {
                DeleteFileRequest dfr = (DeleteFileRequest) msg;
                Path pathToDelete = Paths.get(SERVER_STORAGE + "/" + dfr.getFilename());
                try {
                    Files.delete(pathToDelete);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                refreshServerFileList(ctx);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void refreshServerFileList(ChannelHandlerContext ctx) {
        ListFilesMessage lfm = new ListFilesMessage(createServerFileList());
        ctx.writeAndFlush(lfm);
    }

    private ArrayList<String> createServerFileList() {
        ArrayList<String> serverFiles = new ArrayList<>();
        try {
            Files.list(Paths.get(SERVER_STORAGE)).map(p -> p.getFileName().toString()).forEach(serverFiles::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverFiles;
    }

}
