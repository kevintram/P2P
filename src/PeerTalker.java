import peer.Peer;
import peer.PeerConnection;
import messages.Handshake;
import messages.PeerMessage;

import static messages.PeerMessage.Type.BITFIELD;

/**
 * Deals with talking to peers
 */
public class PeerTalker {

    public PeerTalker() {

    }

    public void run() {
        // Connect to peers with id's less than ours
        int currId = State.us.id;
        while (--currId >= State.startingId) {
            // make a connection
            try {
                PeerConnection conn = connectTo(currId);

                sendHandshake(conn, currId);
                sendBitfield(conn, currId);

            } catch (Exception ignored){
                //not sure if we will need to handle this later
            }

        }
    }

    private PeerConnection connectTo(int id) {
        Peer Peer = State.getPeerById(id);
        PeerConnection conn = new PeerConnection(Peer.hostName, Peer.port);
        Logger.logMakeConnection(State.us.id, id);
        return conn;
    }

    private void sendHandshake(PeerConnection conn, int id) {
        // send a handshake
        conn.send(new Handshake(State.us.id).toByteArray());

        // read response
        byte[] res = new byte[32];
        conn.read(res, 32);

        // check if response is right
        if (new Handshake(res).equals(new Handshake(id))) {
            System.out.println("Shook hands with " + id);
        }
    }

    private void sendBitfield(PeerConnection conn, int id) {
        conn.sendMessage(new PeerMessage(State.bitfieldSize, BITFIELD, State.us.bitField));

        PeerMessage res = conn.readMessage(State.bitfieldSize);
        State.getPeerById(id).bitField = res.payload;
    }

}
