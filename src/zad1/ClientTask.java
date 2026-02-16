package zad1;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ClientTask implements Runnable {
    Client c;
    List<String> reqs;
    boolean ssr;
    private String log;
    private final CompletableFuture<String> completionFuture = new CompletableFuture<>();
    public static ClientTask create(Client c, List<String> reqs, boolean showSendRes) {
        ClientTask ct = new ClientTask();
        ct.c = c;
        ct.reqs = reqs;
        ct.ssr = showSendRes;
        return ct;
    }
    public void run() {
        try {
            c.connect();
            c.send("login " + c.getID());

            for (String req : reqs) {
                String ans = c.send(req);
                if (ssr)
                    System.out.println(ans);
            }
            this.log = c.send("bye and log transfer");
            completionFuture.complete(log);
        } catch (Exception e) {
            completionFuture.completeExceptionally(e);
        }
    }
    public String get() throws ExecutionException, InterruptedException {
        return completionFuture.get();
    }
}
