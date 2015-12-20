package dk.grixie.oauth2.rest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Base64;

public class WebSocket {
    private static final String MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    private String host;
    private int port;
    private String resource;

    private final Socket socket;
    private BufferedInputStream bis;
    private BufferedOutputStream bos;

    public WebSocket(String host, int port, String resource) {
        this.host = host;
        this.port = port;
        this.resource = resource;
        this.socket = new Socket();
    }

    public void connect() throws Exception {
        socket.connect(new InetSocketAddress(host, port));

        String encodedWebSocketKey = new String(Base64.getEncoder().encode(generateWebSocketKey()), "UTF-8");

        bos = new BufferedOutputStream(socket.getOutputStream());

        bos.write(("GET " + resource + " HTTP/1.1\r\n").getBytes("UTF-8"));
        bos.write(("Host: " + host + ":" + port + "\r\n").getBytes("UTF-8"));
        bos.write("Upgrade: websocket\r\n".getBytes("UTF-8"));
        bos.write("Connection: Upgrade\r\n".getBytes("UTF-8"));
        bos.write(("Sec-WebSocket-Key: " + encodedWebSocketKey + "\r\n").getBytes("UTF-8"));
        bos.write("Sec-WebSocket-Version: 13\r\n".getBytes("UTF-8"));
        bos.write("\r\n".getBytes("UTF-8"));
        bos.flush();

        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        bis = new BufferedInputStream(socket.getInputStream());

        int c;
        int state = 0;

        while ((c = bis.read()) != -1) {
            switch (c) {
                case '\r':
                    if (state == 0 || state == 2) {
                        ++state;
                    } else {
                        state = 1;
                    }
                    break;
                case '\n':
                    if (state == 1 || state == 3) {
                        ++state;
                    } else {
                        state = 0;
                    }
                    break;
                default:
                    state = 0;
                    break;
            }

            bas.write(c);

            if (state == 4) {
                break;
            }
        }

        String response = bas.toString("UTF-8");

        MessageDigest digest = MessageDigest.getInstance("SHA1");

        String expectedKey = new String(Base64.getEncoder().
                encode(digest.digest((encodedWebSocketKey + MAGIC).getBytes("UTF-8"))), "UTF-8");

        if (!response.startsWith("HTTP/1.1 101") ||
                !response.contains("Sec-WebSocket-Accept: " + expectedKey + "\r\n")) {
            throw new Exception("OMG the sky is falling");
        }
    }

    public Frame read() throws Exception {
        int op = bis.read();

        if (op > 0) {
            int length=0;

            int temp=bis.read();

            if(temp >= 128) {
                throw new Exception("Masked frame received");
            } else if (temp == 127) {
                throw new IllegalArgumentException("Unsupported frame size");
            } else if(temp == 126) {
                length=temp;
                length<<=8;
                length|=bis.read();
            } else {
                length = temp;
            }

            byte [] data = new byte[length];

            int count = 0;

            while (count < data.length) {
                temp = bis.read(data, count, data.length- count);

                if (temp >= 0) {
                    count += temp;
                } else {
                    throw new Exception("some sort of read error");
                }
            }

            Operation operation = Operation.PING;

            for (Operation o:Operation.values()) {
                if (o.getCode() == (op & 127)) {
                    operation = o;
                }
            }

            return new Frame(operation, (op & 128) != 0, data);
        } else {
            return null;
        }
    }

    public void write(Frame frame) throws Exception {
        bos.write((frame.isLast() ? 128 : 0) | frame.getOperation().getCode());

        if (frame.getData().length < 126) {
            bos.write(128 | frame.getData().length);//mask + length hopefully
        } else if (frame.getData().length < 65536) {
            bos.write(128 | 126);//mask + first extended length
            bos.write(frame.getData().length >> 8);
            bos.write(frame.getData().length & 8);
        } else {
            throw new IllegalArgumentException("Unsupported frame size");
        }

        //write bogus mask
        bos.write(0);//mask byte 1
        bos.write(0);//mask byte 2
        bos.write(0);//mask byte 3
        bos.write(0);//mask byte 4

        bos.write(frame.getData());
        bos.flush();
    }

    public void shutdown() throws Exception {
        socket.getOutputStream().write(128 | Operation.CLOSE.getCode());
        socket.getOutputStream().write(128);//mask + length 0
        socket.getOutputStream().write(0);//mask byte 1
        socket.getOutputStream().write(0);//mask byte 2
        socket.getOutputStream().write(0);//mask byte 3
        socket.getOutputStream().write(0);//mask byte 4

        socket.getOutputStream().flush();
    }

    public void close() throws Exception {
        //TODO: send a close websocket frame
        socket.close();
    }

    private byte[] generateWebSocketKey() throws Exception {
        return "some random value".getBytes("UTF-8");
    }
}
