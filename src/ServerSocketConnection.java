import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketConnection {
    ServerSocket server;
    int port;
    HandlerFactory handlerFactory;

    public ServerSocketConnection(int port, HandlerFactory handlerFactory) {
        this.port = port;
        this.handlerFactory = handlerFactory;
    }

    public void run() {
        try {
            server = new ServerSocket(port);
            try {
                while (true) {
                    handlerFactory.makeHandler(server.accept()).run();
                }
            } finally {
                server.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // client handlers for this server socket connection
    public interface HandlerFactory {
        Runnable makeHandler(Socket socket);
    }
}
