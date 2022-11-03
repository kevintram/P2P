import Peer.Peer;
import Peer.PeerConnection;
import messages.Handshake;
import messages.PeerMessage;

import java.util.Arrays;

import static messages.PeerMessage.Type.BITFIELD;

/**
 * Deals with talking to peers
 */
public class PeerTalker {
    protected final ProcessState state;

    public PeerTalker(ProcessState state) {
        this.state = state;
    }

    public void run() {
        // Connect to peers with id's less than ours
        int currId = state.us.id;
        while (--currId >= state.startingId) {
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
        Peer Peer = state.getPeerById(id);
        PeerConnection conn = new PeerConnection(Peer.hostName, Peer.port);
        Logger.logMakeConnection(state.us.id, id);
        return conn;
    }

    private void sendHandshake(PeerConnection conn, int id) {
        // send a handshake
        conn.send(new Handshake(state.us.id).toByteArray());

        // read response
        byte[] res = new byte[32];
        conn.read(res, 32);

        // check if response is right
        if (new Handshake(res).equals(new Handshake(id))) {
            System.out.println("Shook hands with " + id);
        }
    }

    private void sendBitfield(PeerConnection conn, int id) {
        conn.sendMessage(new PeerMessage(state.bitfieldSize, BITFIELD, state.us.bitField));

        PeerMessage res = conn.readMessage(state.bitfieldSize);
        state.getPeerById(id).bitField = res.payload;
    }

}
