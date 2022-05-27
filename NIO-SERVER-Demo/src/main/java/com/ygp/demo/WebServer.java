package com.ygp.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class WebServer {
    private final static int readCapacity = 1024;
    private final static int writeCapacity = 1024;

    private Selector selector;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;

    public WebServer() {
        try {
            selector = Selector.open();
            readBuffer = ByteBuffer.allocate(readCapacity);
            writeBuffer = ByteBuffer.allocate(writeCapacity);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runServer(String address, int port) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(address, port));
        ssc.register(this.selector, SelectionKey.OP_ACCEPT);

        while(true) {
            int nReady = this.selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while(iterator.hasNext()) {
                SelectionKey event = iterator.next();
                iterator.remove();

                if (event.isValid()) {
                    if(event.isAcceptable()) {
                        this.accept(event);
                    } else if (event.isReadable()) {
                        String str = this.read(event);
                        System.out.println("Client : " + str);
                    } else if (event.isWritable()) {
                        String data = "received";
                        this.write(event ,data);
                    }
                }
            }
        }
    }

    public void accept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel newChannel = ssc.accept();
        newChannel.configureBlocking(false);
        newChannel.register(this.selector, SelectionKey.OP_READ);
    }

    public String read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel)key.channel();
        readBuffer.clear();
        channel.read(readBuffer);
        readBuffer.flip();

        // 读完后，注册感兴趣事件为 Write
        channel.register(selector, SelectionKey.OP_WRITE);
        return readBuffer.asCharBuffer().toString();
    }

    public void write(SelectionKey key, String data) throws IOException{
        SocketChannel channel = (SocketChannel) key.channel();
        writeBuffer.clear();
        writeBuffer.put(data.getBytes("UTF_16"));
        writeBuffer.flip();
        channel.write(writeBuffer);

        key.channel().close();
        key.cancel();
    }

    public static void main(String[] args) {
        WebServer server = new WebServer();
        final int port = 8082;
        final String address = "127.0.0.1";
        try {
            server.runServer(address, port);
        }catch (IOException e) {
            e.printStackTrace();
        }


    }
}
