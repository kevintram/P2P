import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerConnection {
    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public PeerConnection(String hostName, int port) {
        try {
            socket = new Socket(hostName, port);
            in = socket.getInputStream();

            out = socket.getOutputStream();
            out.flush();
        } catch(UnknownHostException e) {
            System.err.println("Tried connecting to an unknown host: " + hostName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PeerConnection(Socket socket) {
        try {
            this.socket = socket;
            in = socket.getInputStream();

            out = socket.getOutputStream();
            out.flush();
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
     * Sends a byte array through the connection
     * @param msg the msg to send
     */
    public void send(byte[] msg) {
        try {
            out.write(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a message from the connection
     * @param buf the buffer into which the data is read
     * @param len the length of the message in bytes
     * @return the total number of bytes read into the buffer
     */
    public int read(byte[] buf, int len) {
        try {
            return in.read(buf, 0, len);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
