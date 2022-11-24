package peer;

import messages.PeerMessage;
import messages.Util;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;

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

    public void sendMessage(PeerMessage msg) {
        send(msg.toByteArray());
    }

    /**
     * Reads from the connection and stores into the buffer
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

    public PeerMessage readMessage() {
        // get payload length
        byte[] lenBuf = new byte[4];
        read(lenBuf, 4);
        int len = Util.byteArrToInt(lenBuf);
        // get type
        byte[] typeBuf = new byte[1];
        read(typeBuf, 1);
        PeerMessage.Type type = PeerMessage.Type.values()[typeBuf[0]];
        // get payload
        byte[] payload = new byte[len];
        read(payload, len);

        return new PeerMessage(type, Optional.of(payload));
    }

    public final Socket getSocket(){
        return this.socket;
    }
}
