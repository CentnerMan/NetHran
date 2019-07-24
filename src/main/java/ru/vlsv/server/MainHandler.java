package ru.vlsv.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.vlsv.common.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static ru.vlsv.common.Tools.MAX_FILE_SIZE;
import static ru.vlsv.common.Tools.createDirIfNotExist;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private static final String SERVER_STORAGE = "server_storage";

    private String currentPath = SERVER_STORAGE + "/";

    private boolean authorization = false; // Авторизация клиента

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
//            AuthService.addUser("admin", "admin"); // Первоначальная инициализация БД
//            AuthService.addUser("test", "test");
//            AuthService.addUser("user", "user");
            createDirIfNotExist(SERVER_STORAGE); //Создаем общую папку на сервере

            if (!authorization) {
                if (msg instanceof AuthorizationRequest) {
                    AuthorizationRequest ar = (AuthorizationRequest) msg;

                    String currentLogin = ar.getName(); // Определяем текущего пользователя

                    if (AuthService.isExist(currentLogin) && ar.getPassword().equals(AuthService.getPass(currentLogin))) {

                        AuthorizationOK ok = new AuthorizationOK();
                        ctx.writeAndFlush(ok);
                        authorization = true;

                        currentPath = currentPath + currentLogin; // Определяем рабочий каталог
                        createDirIfNotExist(currentPath); // Если нет рабочего каталога пользователя - создаем

                    } else {
                        AuthorizationFalse authFalse = new AuthorizationFalse();
                        ctx.writeAndFlush(authFalse);
                    }
                }
            } else {
                if (msg instanceof FileRequest) {
                    FileRequest fr = (FileRequest) msg;
                    if (Files.exists(Paths.get(currentPath + "/" + fr.getFilename()))) {
                        sendFile(ctx, fr.getFilename());
                    }

                } else if (msg instanceof FileMessage) {
                    FileMessage fm = (FileMessage) msg;
                    boolean lastPart = receiveFile(fm);
                    if (lastPart) refreshServerFileList(ctx);

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

    private void sendFile(ChannelHandlerContext ctx, String fileName) {
        if (fileName != null) {
            Path path = Paths.get(currentPath + "/" + fileName);
//            System.out.println("Отправляем файл " + fileName);
            try {
                int numParts = (int) Math.ceil((double) Files.size(path) / MAX_FILE_SIZE);
                byte[] data = new byte[Math.min((int) Files.size(path), MAX_FILE_SIZE)]; // Если файл в один кусок - массив размером с файл
                RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");
                for (int i = 0; i < numParts; i++) {
                    raf.seek((long) i * MAX_FILE_SIZE);
                    int bytesRead = raf.read(data);
                    if (i == numParts - 1) { // обрезаем последний кусок по фактическому размеру
                        byte[] realData = new byte[bytesRead];
                        System.arraycopy(data, 0, realData, 0, bytesRead);
//                        System.out.println("Отправляем последнюю часть " + (i + 1) + " размером " + bytesRead);
                        ctx.writeAndFlush(new FileMessage(path, realData, i, numParts));
                    } else {
                        ctx.writeAndFlush(new FileMessage(path, data, i, numParts));
//                        System.out.println("Отправляем часть " + (i + 1) + " из " + numParts + " размером " + bytesRead);
                    }
                }
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean receiveFile(FileMessage msg) {
        if (msg.getFileName() == null || msg.getData() == null) {
            return false;
        } else {
            Path path = Paths.get(currentPath + "/" + msg.getFileName());
            try {
                if (!Files.exists(path)) {
                    Files.createFile(path);
                }
                RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");
                raf.seek((long) msg.getPartNum() * MAX_FILE_SIZE);
                raf.write(msg.getData());
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (msg.getPartNum() == msg.getPartCount() - 1);
    }
}
