package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Client {
    private final String HOST;
    private final int PORT;
    private final String ID;
    private SocketChannel sChannel;
    public Client(String host, int port, String id) {
        HOST = host;
        PORT = port;
        ID = id;
    }

    public void connect() {
        try {
            sChannel = SocketChannel.open();
            sChannel.connect(new InetSocketAddress(HOST, PORT));
            sChannel.configureBlocking(false);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String send(String req) {
        try(Selector selector = Selector.open()) {
            sChannel.write(ByteBuffer.wrap(req.getBytes(StandardCharsets.UTF_8)));

            sChannel.register(selector, SelectionKey.OP_READ);

            while(true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for(SelectionKey key : selectionKeys) {
                    if(key.isReadable()) {
                        SocketChannel readChannel = (SocketChannel) key.channel();
                        ByteBuffer buff = ByteBuffer.allocateDirect(1024);

                        int bytesRead = readChannel.read(buff);
                        if(bytesRead > 0) {
                            buff.flip();

                            byte[] bytes = new byte[buff.remaining()];
                            buff.get(bytes);
                            return new String(bytes, StandardCharsets.UTF_8);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getID() {
        return ID;
    }
}
