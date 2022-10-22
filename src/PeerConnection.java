import messages.Util;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
//TODO set up a thread system so we can download from multiple clients and upload to multiple
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
            throw new RuntimeException();
        } catch (ConnectException e) {
            System.err.println("Connection refused. Need to start peer at hostname " + hostName + " and port " + port);
            throw new RuntimeException();
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

    public final Socket getSocket(){
        return this.socket;
    }
}
