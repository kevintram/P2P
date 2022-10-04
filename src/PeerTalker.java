import messages.Handshake;

/**
 * Deals with talking to peers
 */
public class PeerTalker {
    protected PeerState state;

    public void run() {
        // Connect to peers with id's less than ours
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
}
