package zad1;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Server {
    private final String HOST;
    private final int PORT;
    private StringBuilder serverLog;
    private Map<String, StringBuilder> clientLog;
    private volatile boolean isRunning = true;
    private ServerSocketChannel ssc;
    private Selector selector;
    private Thread thread;

    public Server(String host, int port) {
        HOST = host;
        PORT = port;
        serverLog = new StringBuilder();
        clientLog = new LinkedHashMap<>();

        Locale.setDefault(Locale.US);
    }

    public void startServer() {
        thread = new Thread(() -> {
            try {
                ssc = ServerSocketChannel.open();
                selector = Selector.open();

                ssc.bind(new InetSocketAddress(HOST, PORT));
                ssc.configureBlocking(false);
                ssc.register(selector, SelectionKey.OP_ACCEPT);

                Map<SocketAddress, String> ipIdClientsMap = new HashMap<>();

                while(isRunning) {

                    try {
                        selector.select();
                        Set<SelectionKey> sKeys = selector.selectedKeys();

                        for (SelectionKey sKey : sKeys) {
                            if (sKey.isAcceptable()) {
                                SocketChannel client = ssc.accept();
                                client.configureBlocking(false);
                                client.register(selector, SelectionKey.OP_READ);
                            }
                            if (sKey.isReadable()) {
                                SocketChannel channel = (SocketChannel) sKey.channel();
                                String time = LocalTime.now().toString();

                                ByteBuffer messageBuffer = ByteBuffer.allocateDirect(1024);
                                if (channel.read(messageBuffer) == -1) {
                                    sKey.cancel();
                                    continue;
                                }
                                messageBuffer.flip();
                                String message = StandardCharsets.UTF_8.decode(messageBuffer).toString();

                                String answer = "";
                                if (Pattern.compile("^login\\s+(\\w+)$").matcher(message).matches()) {
                                    String id = message.split(" ", 2)[1];
                                    ipIdClientsMap.put(channel.getRemoteAddress(), id);
                                    clientLog.putIfAbsent(id, new StringBuilder("=== " + id + " log start ===\n"));

                                    answer = "logged in";
                                }
                                if (Pattern.compile(
                                        "^\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2})?" +
                                                " \\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2})?$").matcher(message).matches()
                                ) {
                                    String[] dates = message.split(" ");
                                    answer = Time.passed(dates[0], dates[1]);
                                    serverLog.append(ipIdClientsMap.get(channel.getRemoteAddress()))
                                            .append(" request at ").append(time).append(": ").append('"')
                                            .append(message).append('"').append("\n");

                                    clientLog.get(ipIdClientsMap.get(channel.getRemoteAddress())).append("Request: ")
                                            .append(message).append('\n').append("Result:")
                                            .append("\n").append(answer).append("\n");
                                } else if (!answer.equals("")) {

                                    serverLog.append(ipIdClientsMap.get(channel.getRemoteAddress()))
                                            .append(" ").append(answer).append(" at ")
                                            .append(time).append("\n");
                                    clientLog.get(ipIdClientsMap.get(channel.getRemoteAddress())).append(answer + "\n");
                                }

                                if (message.startsWith("bye")) {
                                    answer = "logged out";

                                    serverLog.append(ipIdClientsMap.get(channel.getRemoteAddress()))
                                            .append(" ").append(answer).append(" at ")
                                            .append(time).append("\n");
                                    clientLog.get(ipIdClientsMap.get(channel.getRemoteAddress()))
                                            .append(answer).append("\n").append("=== ")
                                            .append(ipIdClientsMap.get(channel.getRemoteAddress()))
                                            .append(" log end ===\n");
                                }
                                if (message.equals("bye and log transfer"))
                                    answer = clientLog.get(ipIdClientsMap.get(channel.getRemoteAddress())).toString();

                                ByteBuffer answerBuff = ByteBuffer.wrap(answer.getBytes(StandardCharsets.UTF_8));
                                while (answerBuff.hasRemaining()) {
                                    channel.write(answerBuff);
                                }
                                if (message.startsWith("bye")) {
                                    sKey.cancel();
                                    channel.close();
                                }
                            }
                        }
                        sKeys.clear();
                    } catch(ClosedSelectorException ex) {
                        isRunning = false;
                    }
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }
    public void stopServer() {
//        synchronized (Lock) {
//            if (!isRunning)
//                return;
//            isRunning = false;
//            executorService.shutdownNow();
//
//            try {
//                if(selector.isOpen()) {
////                    selector.wakeup();
//                }
//                if(ssc.isOpen()) {
//                    ssc.close();
//                }
//                if(selector.isOpen())
//                    selector.close();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
        thread.interrupt();

        try {
            ssc.close();
            selector.close();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        isRunning = false;
    }
    public String getServerLog() {
        return serverLog.toString();
    }
}
