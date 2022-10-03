import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketConnection {
    private Socket socket;
    private OutputStream out;
    private InputStream in;

    private final String hostName;
    private final int port;

    public SocketConnection(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public void run() {
        connect();
    }

    private void connect() {
        try {
            socket = new Socket(hostName, port);
            in = socket.getInputStream();
        } catch(UnknownHostException e) {
            System.err.println("Tried connecting to an unknown host: " + hostName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

    /**
     * Sends a message through the connection
     */
    public void send(byte[] msg) {
        try {
            out = socket.getOutputStream();
            out.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read a handshake message
     * @return the handshake message as a byte array
     */
    public byte[] readHandshake() {
        try {
            byte[] handshake = new byte[32];
            in.read(handshake);
            return handshake;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
