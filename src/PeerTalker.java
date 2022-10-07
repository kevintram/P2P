import messages.Handshake;

/**
 * Deals with talking to peers
 */
public class PeerTalker {
    protected final PeerState state;

    public PeerTalker(PeerState state) {
        this.state = state;
    }

    public void run() {
        // Connect to peers with id's less than ours
        int id = state.us.id;
        while (--id >= 1001) {
            // make a connection
            try {
                Peer Peer = state.getPeerById(id);
                PeerConnection conn = new PeerConnection(Peer.hostName, Peer.port);
                Peer.connection = conn;
                System.out.println("Made a connection to " + id);

                // send a handshake
                conn.send(new Handshake(state.us.id).toByteArray());

                // read response
                byte[] res = new byte[32];
                conn.read(res, 32);

                // check if response is right
                if (new Handshake(res).equals(new Handshake(id))) {
                    System.out.println("Shook hands with " + id);
                }
            } catch (Exception ignored){}

        }
    }
}
