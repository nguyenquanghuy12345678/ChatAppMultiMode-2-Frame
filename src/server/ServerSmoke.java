package server;

public class ServerSmoke {
    public static void main(String[] args) throws InterruptedException {
        ChatServer server = new ChatServer(0);
        server.start();
        // Wait a bit for server to start listening
        Thread.sleep(1000);
        if (server.isRunning()) {
            server.log("Smoke test: server is running (port=" + server.getPort() + ")");
        } else {
            server.log("Smoke test: server failed to start");
        }
        server.stop();
        Thread.sleep(500);
        server.log("Smoke test: server stopped");
    }
}
