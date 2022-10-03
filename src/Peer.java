import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Peer {
    private HashMap<Integer, PeerInfo> peerInfo;
    private final int peerID;
    private final int port;

    private HashMap<Integer, SocketConnection> clients;
    private ArrayList<Integer> unchoked;

    public Peer(int peerID, int port, List<PeerInfo> peerInfo) {
        this.peerID = peerID;
        this.port = port;

        this.peerInfo = new HashMap<>();
        for (PeerInfo p : peerInfo) {
            this.peerInfo.put(p.peerID, p);
        }

        clients = new HashMap<>();
        unchoked = new ArrayList<>();
    }

    public void run() {
        connectToPrevPeers();
        new ServerSocketConnection(port, new ClientHandlerFactory()).run();
    }

    private void connectToPrevPeers() {
        int curr = peerID;
        while (--curr >= 1001) {
            connectTo(curr);
            if (shakeHands(curr)) {
                System.out.println("Hand shake successful");
            }
        }
    }

    private void connectTo(int peerID) {
        PeerInfo p = peerInfo.get(peerID);
        SocketConnection c = new SocketConnection(p.hostName, p.port);
        c.run();
        clients.put(peerID, c);
    }

    /**
     * Sends a handshake message to the peer with the corresponding peerID
     * @return the success of the handshake
     */
    public boolean shakeHands(int peerId) {
        SocketConnection peer = clients.get(peerId);

        if (peer == null) {
            throw new RuntimeException("ERROR: Tried shaking hands with an invalid peerID!");
        }

        peer.send(getHandshakeMessage());
        byte[] returnHandshake = peer.readHandshake();

        // check if return message has correct header and peerId
        byte[] header = Arrays.copyOf(returnHandshake, 18);
        boolean hasCorrectHeader = Arrays.equals(header, "P2PFILESHARINGPROJ".getBytes());

        byte[] peerIdBuffer = Arrays.copyOfRange(returnHandshake, 28, 32);
        int resPeerId = fromByteArray(peerIdBuffer);
        boolean hasCorrectId = resPeerId == peerId;

        return hasCorrectHeader && hasCorrectId;
    }

    /**
     * @return the byte array of this peer's handshake message
     */
    private byte[] getHandshakeMessage() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // write header
            baos.write("P2PFILESHARINGPROJ".getBytes());
            // write 10-byte 0 bits
            for (int i = 0; i < 10; i++) {
                baos.write(0);
            }
            // write peer id
            baos.write(toByteArray(peerID));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return baos.toByteArray();
    }

    public class ClientHandlerFactory implements ServerSocketConnection.HandlerFactory {
        @Override
        public Runnable makeHandler(Socket socket) {
            return new ClientHandler(socket);
        }
    }

    public class ClientHandler implements Runnable {
        private Socket client;
        private InputStream in;
        private OutputStream out;

        public ClientHandler(Socket client)  {
            this.client = client;
            try {
                in = client.getInputStream();
                out = client.getOutputStream();
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // read and reciprocate handshake
                byte[] header = new byte[18];
                in.read(header, 0, 18);
                boolean isHandshake = Arrays.equals(
                        header,
                        "P2PFILESHARINGPROJ".getBytes()
                );

                if (isHandshake) {
                    out.write(getHandshakeMessage());
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // get int from array of 4 bytes
    public static int fromByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF) << 0 );
    }

    // convert int to array of 4 bytes
    public static byte[] toByteArray(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }
}
