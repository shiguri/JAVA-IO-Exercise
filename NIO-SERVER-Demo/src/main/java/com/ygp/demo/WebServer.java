package com.ygp.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class WebServer {
    private final static String host = "127.0.0.1";
    private final static int port = 8081;
    private final static int readCapacity = 1024;
    private final static int writeCapacity = 1024;

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();

        /**
         * 创建一个 ServerSocketChannel 并注册到Selector中。
         * 指定的触发事件为 ACCEPT，即一个新的连接请求。
         */
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(host, port));
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        ByteBuffer readBuffer = ByteBuffer.allocate(readCapacity);
        ByteBuffer writeBuffer = ByteBuffer.allocate(writeCapacity);

        while(true) {
            int nReady = selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while(iterator.hasNext()) {
                SelectionKey event = iterator.next();
                iterator.remove();

                if (event.isAcceptable()) {
                    /**
                     * 创建一个新的连接，并将新连接绑定到 Selector 上，
                     * 新连接只对 read 事件感兴趣。
                     */
                    SocketChannel newSocketChannel = ssc.accept();
                    newSocketChannel.configureBlocking(false);
                    newSocketChannel.register(selector, SelectionKey.OP_READ);
                } else if (event.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) event.channel();
                    readBuffer.clear();
                    socketChannel.read(readBuffer);
                    readBuffer.flip();
                    System.out.println( new String(readBuffer.array()) );
                    event.interestOps(SelectionKey.OP_WRITE);
                } else if (event.isWritable()) {
                    SocketChannel socketChannel = (SocketChannel) event.channel();
                    writeBuffer.rewind();
                    socketChannel.write(writeBuffer);
                    event.interestOps(SelectionKey.OP_READ);
                }

            }
        }
    }

}
