package peer;

import messages.PeerMessage;
import messages.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class PeerConnection {

    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public PeerConnection(String hostName, int port) {
        while (socket == null || !socket.isConnected()) {
            try {
                socket = new Socket(hostName, port);
                in = socket.getInputStream();
                out = socket.getOutputStream();
                out.flush();
            } catch(UnknownHostException e) {
                System.err.println("Tried connecting to an unknown host: " + hostName);
                throw new RuntimeException();
            } catch (ConnectException ignored) {

            } catch(IOException e) {
                e.printStackTrace();
            }
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
            out.flush();
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

    public void sendMessage(PeerMessage msg) throws InterruptedException {
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
            synchronized (in){
                return in.read(buf, 0, len);
            }
        }catch (SocketException e){
            return -1;
        }catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public PeerMessage readMessage() {
        // get payload length

        byte[] lenBuf = new byte[4];
        if(read(lenBuf, 4) == -1) return null;
        int len = Util.byteArrToInt(lenBuf);
        // get type
        byte[] typeBuf = new byte[1];
        if(read(typeBuf, 1) == -1) return null;
        PeerMessage.Type type = PeerMessage.Type.values()[typeBuf[0]];
        // get payload
        byte[] payload = new byte[len];
        if(read(payload, len) == -1) return null;

        return new PeerMessage(type, payload);
    }

    public final Socket getSocket(){
        return this.socket;
    }
}
