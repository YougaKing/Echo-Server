package com.youga.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.handler.timeout.IdleStateEvent;
import netty.echo.EchoCommon.Target;
import netty.echo.EchoFile;
import netty.echo.EchoMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;


/**
 * Created by Lison-Liou on 5/17/2016.
 */
public class EchoServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String FILE_SAVE_PATH = "D:";
    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    // 失败计数器：未收到client端发送的ping请求
    private int mUnRecPingTimes = 0;
    // 设置6秒检测chanel是否接受过心跳数据
    static final int READ_WAIT_SECONDS = 6;
    // 定义客户端没有收到服务端的pong消息的最大次数
    private static final int MAX_UN_REC_PING_TIMES = 3;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        Channel incoming = ctx.channel();

        if (msg instanceof EchoFile) {
            EchoFile echo = (EchoFile) msg;
            String path = FILE_SAVE_PATH + File.separator + echo.fileName;
            File file = new File(path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
//            randomAccessFile.seek(countPackage * DATA_LENGTH - DATA_LENGTH);
            randomAccessFile.write(echo.bytes);
            randomAccessFile.close();

            EchoMessage message = EchoMessage.buildMessage("SERVER - GOT YOUR File " + echo.fileName + " fileSize:" + echo.fileSize, Target.SERVER);
            System.out.println("RECEIVED: " + ctx.channel().remoteAddress() + " " + echo.fileName + " " + echo.fileSize);
            incoming.writeAndFlush(message);
        } else if (msg instanceof EchoMessage) {
            EchoMessage message = (EchoMessage) msg;
            if (message.target == Target.HEART_BEAT) {
                // 计数器清零
                mUnRecPingTimes = 0;
            } else {
                message = EchoMessage.buildMessage("SERVER - GOT YOUR MESSAGE " + message.getMessage(), Target.SERVER);
            }
            System.out.println("RECEIVED: " + ctx.channel().remoteAddress() + " " + message.target.getDescribe() + " " + message.getMessage());
            incoming.writeAndFlush(message);
        } else {
            System.out.println("RECEIVED: " + msg.toString());
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        Channel incoming = ctx.channel();
        System.out.println("SYSTEM CHANNEL ADDED: " + incoming.remoteAddress() + "-->SYSTEM CHANNEL SIZE: " + channels.size());
        channels.add(incoming);

        EchoMessage message = EchoMessage.buildMessage("YOU ONLINE", Target.SERVER);
        incoming.writeAndFlush(message);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        Channel incoming = ctx.channel();
        System.out.println("SYSTEM CHANNEL REMOVED: " + incoming.remoteAddress() + "-->SYSTEM CHANNEL SIZE: " + channels.size());
        channels.remove(incoming);

        EchoMessage message = EchoMessage.buildMessage("YOU OFFLINE", Target.SERVER);
        incoming.writeAndFlush(message);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // IdleStateHandler 所产生的 IdleStateEvent 的处理逻辑.
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                    // 失败计数器次数大于等于3次的时候，关闭链接，等待client重连
                    if (mUnRecPingTimes >= MAX_UN_REC_PING_TIMES) {
                        // 连续超过N次未收到client的ping消息，那么关闭该通道，等待client重连
                        ctx.channel().close();
                    } else {
                        // 失败计数器加1
                        mUnRecPingTimes++;
                    }
                    System.err.println("---READER_IDLE---mUnRecPingTimes:" + mUnRecPingTimes);
                    break;
                case WRITER_IDLE:
                    System.err.println("---WRITER_IDLE---");
                    break;
                case ALL_IDLE:
                    System.err.println("---ALL_IDLE---");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        EchoMessage message = EchoMessage.buildMessage(cause.getMessage(), Target.SERVER);
        ctx.writeAndFlush(message);
        ctx.close();
    }
}
