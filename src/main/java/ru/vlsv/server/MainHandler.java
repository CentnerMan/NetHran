package ru.vlsv.server;

import ru.vlsv.common.FileMessage;
import ru.vlsv.common.FileRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
            }
            if (msg instanceof FileMessage) {
                // Что делать если прилетел файл ??
                FileMessage fm = (FileMessage) msg;
                Path pathToNewFile = Paths.get(SERVER_STORAGE + "/" + fm.getFilename());
                if (Files.exists(pathToNewFile)) {
                    System.out.println("Файл с именем " + fm.getFilename() + " уже существует");
                } else {
                    Files.write(Paths.get(SERVER_STORAGE + "/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
//                    refreshRemoteFilesList();
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
