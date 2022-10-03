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

        ServerSocketConnection s = new ServerSocketConnection(port, new ClientHandlerFactory());
        s.run();
    }

    private void connectToPrevPeers() {
        int curr = peerID;
        while (--curr >= 1001) {
            connect(curr);
            if (shakeHands(curr)) {
                System.out.println("Hand shake successful");
            }
        }
    }

    private void connect(int peerID) {
        System.out.println("Connecting to peer " + peerID);
        PeerInfo p = peerInfo.get(peerID);
        SocketConnection c = new SocketConnection(p.hostName, p.port);
        c.run();
        clients.put(peerID, c);
    }

    /**
     * Tries to shake hands with peer
     * @return the success of the handshake
     */
    public boolean shakeHands(int peerID) {
        System.out.println("Shaking hands!");
        SocketConnection peer = clients.get(peerID);
        peer.send(makeHandshake());
        byte[] res = peer.readHandshake();

        System.out.println("Got something back!");

        byte[] header = Arrays.copyOf(res, 18);
        boolean hasCorrectHeader = Arrays.equals(header, "P2PFILESHARINGPROJ".getBytes());

        byte[] bufferArr = Arrays.copyOfRange(res, 28, 32);
        int resPeerId = fromByteArray(bufferArr);
        boolean hasCorrectId = resPeerId == peerID;

        System.out.println(hasCorrectHeader);
        System.out.println(hasCorrectId);
        System.out.println(resPeerId);
        System.out.println(Arrays.toString(res));
        System.out.println(Arrays.toString(bufferArr));

        return hasCorrectHeader && hasCorrectId;
    }

    private byte[] makeHandshake() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            baos.write("P2PFILESHARINGPROJ".getBytes());
            for (int i = 0; i < 10; i++) {
                baos.write(0);
            }
            System.out.println(peerID);
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

        public ClientHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                in = client.getInputStream();
                out = client.getOutputStream();
                out.flush();

                System.out.println("Server is reading!");

                byte[] header = new byte[18];
                in.read(header, 0, 18);
                boolean isHandshake = Arrays.equals(
                        header,
                        "P2PFILESHARINGPROJ".getBytes()
                );

                System.out.println(isHandshake);

                if (isHandshake) {
                    System.out.println("Reciprocating the handshake!");
                    out.write(makeHandshake());
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // array of 4 bytes to an int
    public static int fromByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF) << 0 );
    }

    // an int to an array of 4 bytes
    public static byte[] toByteArray(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }
}
