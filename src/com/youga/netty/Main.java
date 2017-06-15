package com.youga.netty;

import com.youga.netty.server.EchoServer;

/**
 * Created by Youga on 2017/6/15.
 */
public class Main {

    /**
     * 应用程序入口.
     */
    public static void main(String[] args) {

        int port ;
        if (args != null && args.length != 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        new EchoServer().initialize();
    }
}
