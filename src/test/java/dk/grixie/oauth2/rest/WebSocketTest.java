package dk.grixie.oauth2.rest;

import org.junit.Test;

public class WebSocketTest {

    @Test
    public void test() throws Exception {
        WebSocket webSocket = new WebSocket("localhost", 8080, "/oauth2-rest/websocket/echo");

        webSocket.connect();

        Frame frame = new Frame(Operation.TEXT, true, "hello".getBytes("UTF-8"));

        webSocket.write(frame);

        frame = webSocket.read();

        int closeCode = 1000;

        byte[] reason = {(byte) (closeCode >> 8), (byte) (closeCode & 255), 'g', 'o', 'o', 'd', 'b', 'y', 'e'};

        Frame close = new Frame(Operation.CLOSE, true, reason);

        webSocket.write(close);

        close = webSocket.read();

        int closeCode2 = close.getData()[0];
        closeCode2 <<= 8;
        closeCode2 |= ((int) (close.getData()[1]) & 255);

        String reason2 = new String(close.getData(), 2, close.getData().length - 2, "UTF-8");

        webSocket.close();
    }
}
