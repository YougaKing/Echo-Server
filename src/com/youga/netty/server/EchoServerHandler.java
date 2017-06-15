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

import java.util.Arrays;


/**
 * Created by Lison-Liou on 5/17/2016.
 */
public class EchoServerHandler extends SimpleChannelInboundHandler<Object> {

    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final byte PING_MSG = 1;
    private static final byte PONG_MSG = 2;
    private int heartbeatCount;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        Channel incoming = ctx.channel();

        if (msg instanceof EchoFile) {
//            EchoFile ef = (EchoFile) msg;
//            int SumCountPackage = ef.getSumCountPackage();
//            int countPackage = ef.getCountPackage();
//            byte[] bytes = ef.getBytes();
//            String file_name = ef.getFile_name();
//
//            String path = FILE_SAVE_PATH + File.separator + file_name;
//            File file = new File(path);
//            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
//            randomAccessFile.seek(countPackage * DATA_LENGTH - DATA_LENGTH);
//            randomAccessFile.write(bytes);
//
//            System.out.println("SYSTEM TOTAL PACKAGE COUNT：" + ef.getSumCountPackage());
//            System.out.println("SYSTEM NOT IS THE " + countPackage + "th PACKAGE");
//            System.out.println("SYSTEM PACKAGE COUNT: " + bytes.length);
//
//            countPackage = countPackage + 1;
//
//            if (countPackage <= SumCountPackage) {
//                ef.setCountPackage(countPackage);
//                ctx.writeAndFlush(ef);
//                randomAccessFile.close();
//
//                ctx.writeAndFlush("SYSTEM " + countPackage + " UPLOADED");
//            } else {
//                randomAccessFile.close();
//                ctx.close();
//                ctx.writeAndFlush("SYSTEM " + ef.getFile_name() + " UPLOAD FINISHED");
//            }
        } else if (msg instanceof EchoMessage) {
            EchoMessage message = (EchoMessage) msg;

            String str = message.getMessage();
            System.out.println("RECEIVED: " + ctx.channel().remoteAddress() + " " + str);

            message = EchoMessage.buildMessage("SYSTEM - GOT YOUR MESSAGE" + str, Target.SERVER);
            incoming.writeAndFlush(message);
        } else if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            System.err.println(Arrays.toString(byteBuf.array()));
            if (byteBuf.getByte(4) == PING_MSG) {
                sendPongMsg(ctx);
            } else if (byteBuf.getByte(4) == PONG_MSG) {
                System.out.println(" get pong msg from " + ctx.channel().remoteAddress());
            } else {
                handleData(ctx, byteBuf);
            }
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
                    System.err.println("---client " + ctx.channel().remoteAddress().toString() + " reader timeout, close it---");
                    ctx.close();
                    break;
                case WRITER_IDLE:
                    break;
                case ALL_IDLE:
                    break;
                default:
                    break;
            }
        }
    }

    protected void handleData(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        Channel incoming = ctx.channel();
        byte[] data = new byte[byteBuf.readableBytes() - 5];
        byteBuf.skipBytes(5);
        byteBuf.readBytes(data);

        EchoMessage message = EchoMessage.buildMessage(data, Target.SERVER);
        incoming.writeAndFlush(message);
        System.out.println(message.getMessage());
    }

    protected void sendPingMsg(ChannelHandlerContext context) {
        ByteBuf buf = context.alloc().buffer(5);
        buf.writeInt(5);
        buf.writeByte(PING_MSG);
        buf.retain();
        context.writeAndFlush(buf);
        heartbeatCount++;
        System.out.println(" sent ping msg to " + context.channel().remoteAddress() + ", count: " + heartbeatCount);
    }

    private void sendPongMsg(ChannelHandlerContext context) {
        ByteBuf buf = context.alloc().buffer(5);
        buf.writeInt(5);
        buf.writeByte(PONG_MSG);
        context.channel().writeAndFlush(buf);
        heartbeatCount++;
        System.out.println(" sent pong msg to " + context.channel().remoteAddress() + ", count: " + heartbeatCount);
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
