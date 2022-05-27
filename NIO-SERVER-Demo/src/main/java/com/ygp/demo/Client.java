package com.ygp.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class Client {
    private final static int WRITE_CAPACITY = 1024;
    private final static int READ_CAPACITY = 1024;

    private Selector selector;
    // 不同于Server， Client 只需要维持一个 SocketChannel就可以了。
    private SocketChannel socket;

    public Client() {
        try {
            this.selector = Selector.open();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientConnectInit(String address, int port) throws IOException {
        socket = SocketChannel.open();
        socket.connect(new InetSocketAddress(address, port));
        socket.configureBlocking(false);
        // 该 Channel 在 read 事件时触发。
        socket.register(selector, SelectionKey.OP_WRITE);
    }

    public void write(SelectionKey key, String data) throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(WRITE_CAPACITY);
        buffer.clear();
        buffer.put(data.getBytes("UTF_16"));
        buffer.flip();
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(buffer);
        key.interestOps(SelectionKey.OP_READ);
    }

    public String read(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(READ_CAPACITY);
        SocketChannel channel = (SocketChannel) key.channel();

        int count = channel.read(buffer);
        // 没有数据
        if ( -1 == count) {
            key.cancel();
            return "";
        }
        buffer.flip();
        String result = buffer.asCharBuffer().toString();
        key.interestOps(SelectionKey.OP_WRITE);
        return result;
    }

    public static void main(String[] args) throws IOException {
        final Client client = new Client();
        client.clientConnectInit("127.0.0.1", 8082);

        /**
         * 创建辅助线程，负责读取 Server 通信。
         */
        Thread readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        int nReady = client.selector.select();
                        Set<SelectionKey> keys = client.selector.selectedKeys();
                        Iterator<SelectionKey> iterator = keys.iterator();
                        int index = 1;

                        while (iterator.hasNext()) {
                            SelectionKey event = iterator.next();
                            iterator.remove();
                            if (event.isValid()) {
                                if (event.isReadable()) {
                                    String s = client.read(event);
                                    System.out.println("Server : " + s);
                                } else if (event.isWritable()) {
                                    client.write(event, index + " data");
                                    index += 1 ;
                                }
                            }

                        }
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        readThread.start();
    }
}
