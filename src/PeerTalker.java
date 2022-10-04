import messages.Handshake;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Deals with talking to peers
 */
public class PeerTalker {
    protected PeerState state;

    public void run() {
        connectToPrevPeers();
        runServer();
    }

    /**
     * Connect to peers with id's less than ours
     */
    private void connectToPrevPeers() {
        int currId = state.us.id;
        while (--currId >= 1001) {
            // make a connection
            Peer peer = state.getPeerById(currId);
            PeerConnection conn = new PeerConnection(peer.hostName, peer.port);
            peer.connection = conn;
            System.out.println("Made a connection to " + currId);

            // send a handshake
            conn.send(new Handshake(state.us.id).toByteArray());

            // read response
            byte[] res = new byte[32];
            conn.read(res, 32);

            // check if response is right
            if (new Handshake(res).equals(new Handshake(currId))) {
                System.out.println("Shook hands with " + currId);
            }
        }
    }

    /**
     * Runs a server at our port
     */
    private void runServer() {
        try {
            ServerSocket server = new ServerSocket(state.us.port);
            try {
                while (true) {
                    new PeerResponder(server.accept(), state).run();
                }
            } finally {
                server.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
