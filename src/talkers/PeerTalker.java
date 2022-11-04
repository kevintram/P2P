package talkers;

import peer.Peer;
import peer.PeerConnection;
import messages.Handshake;
import messages.PeerMessage;

import java.util.ArrayList;

import static messages.PeerMessage.Type.*;

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
                seeIfInterested(conn, currId);
            } catch (Exception ignored){
                //not sure if we will need to handle this later
            }

        }
    }

    private PeerConnection connectTo(int id) {
        Peer Peer = State.getNeighborById(id);
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

        PeerMessage res = conn.readMessage();
        State.getNeighborById(id).bitField = res.payload;
    }

    protected void seeIfInterested(PeerConnection conn, int id) {
        ArrayList<Integer> newPieces = newPiecesFrom(State.getNeighborById(id).bitField);

        PeerMessage.Type interest = (newPieces.isEmpty())? NOT_INTERESTED : INTERESTED;
        conn.sendMessage(new PeerMessage(0, interest, new byte[0]));
    }

    /**
     * @param them bitfield of peer we're looking at
     * @return an arraylist of block indices for new pieces (ones we don't have). It's empty if they have nothing new.
     */
    protected ArrayList<Integer> newPiecesFrom(byte[] them){
        ArrayList<Integer> indices = new ArrayList<>();
        byte[] us = State.us.bitField;

        for(int i = 0; i < State.bitfieldSize; i++) {
            if(them[i] == 1 && us[i] == 0){
                indices.add(i);
            }
        }

        return indices;
    }

}
