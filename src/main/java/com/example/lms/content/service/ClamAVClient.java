package com.example.lms.content.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClamAVClient {

    private String host;
    private int port;

    public ClamAVClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public byte[] scan(byte[] fileData) throws IOException {
        try (Socket socket = new Socket(host, port);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            out.write("zINSTREAM\0".getBytes());
            out.flush();

            byte[] chunk = new byte[2048];
            int bytesRead;
            while ((bytesRead = fileData.length) > 0) {
                out.write((byte) (bytesRead >> 24));
                out.write((byte) (bytesRead >> 16));
                out.write((byte) (bytesRead >> 8));
                out.write((byte) bytesRead);
                out.write(fileData, 0, bytesRead);
            }

            out.write(new byte[4]);
            out.flush();

            byte[] reply = new byte[1024];
            in.read(reply);
            return reply;
        }
    }

    public static boolean isCleanReply(byte[] reply) {
        return new String(reply).contains("OK");
    }
}