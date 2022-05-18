package com.ygp;

import sun.nio.cs.StandardCharsets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class PipeExercise {
    public static void main(String[] args) throws IOException {
        final Pipe pipe = Pipe.open();
        final String s = "use pipe to transmit data from one thread to another thread.";
        Thread writeThread = new Thread() {
            public void run() {
                try {
                    writeDataToPipe(pipe, s);
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread readThread = new Thread() {
            public void run(){
                try {
                    readDataFromPipe(pipe);
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        writeThread.run();
        readThread.run();

    }

    public static void writeDataToPipe(Pipe pipe, String data) throws IOException{
        Pipe.SinkChannel sink = pipe.sink();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(data.getBytes("UTF_16"));
        buffer.flip();
        while(buffer.hasRemaining()) {
            sink.write(buffer);
        }
    }

    public static void readDataFromPipe(Pipe pipe) throws IOException{
        Pipe.SourceChannel source = pipe.source();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while(source.read(buffer) > 0) {
            buffer.flip();
            String s = buffer.asCharBuffer().toString();
            System.out.println(s);
            buffer.clear();
        }
    }
}
