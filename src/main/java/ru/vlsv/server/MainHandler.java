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

    private String currentPath = SERVER_STORAGE + "/";

    private boolean authorization = false; // Авторизация клиента

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
//            AuthService.addUser("admin", "admin");
//            AuthService.addUser("test", "test");
//            AuthService.addUser("user", "user");
            if (!authorization) {
                if (msg instanceof AuthorizationRequest) {
                    AuthorizationRequest ar = (AuthorizationRequest) msg;

                    String currentLogin = ar.getName(); // Определяем текущего пользователя

                    if (AuthService.isExist(currentLogin) && ar.getPassword().equals(AuthService.getPass(currentLogin))) {

                        AuthorizationOK ok = new AuthorizationOK();
                        ctx.writeAndFlush(ok);
                        authorization = true;

                        currentPath = currentPath + currentLogin; // Определяем рабочий каталог

                        // Если нет рабочего каталога - создаем
                        File userDir = new File(currentPath);
                        if (!userDir.exists()) {
                            if (userDir.mkdir()) {
                                System.out.println("Папка пользователя создана");
                            } else System.out.println("Ошибка создания папки");
                        }
                    } else {
                        AuthorizationFalse authFalse = new AuthorizationFalse();
                        ctx.writeAndFlush(authFalse);
                    }
                }
            } else {
                if (msg instanceof FileRequest) {
                    FileRequest fr = (FileRequest) msg;
                    if (Files.exists(Paths.get(currentPath + "/" + fr.getFilename()))) {
                        FileMessage fm = new FileMessage(Paths.get(currentPath + "/" + fr.getFilename()));
                        ctx.writeAndFlush(fm);
                    }

                } else if (msg instanceof FileMessage) {
                    FileMessage fm = (FileMessage) msg;
                    Path pathToNewFile = Paths.get(currentPath + "/" + fm.getFilename());
                    if (Files.exists(pathToNewFile)) {
                        System.out.println("Файл с именем " + fm.getFilename() + " уже существует");
                    } else {
                        Files.write(Paths.get(currentPath + "/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                    }
                    refreshServerFileList(ctx);

                } else if (msg instanceof ListFilesRequest) {
                    refreshServerFileList(ctx);

                } else if (msg instanceof DeleteFileRequest) {
                    DeleteFileRequest dfr = (DeleteFileRequest) msg;
                    Path pathToDelete = Paths.get(currentPath + "/" + dfr.getFilename());
                    try {
                        Files.delete(pathToDelete);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    refreshServerFileList(ctx);
                }
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
            Files.list(Paths.get(currentPath)).map(p -> p.getFileName().toString()).forEach(serverFiles::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverFiles;
    }

}
