package talkers;

import peer.Neighbor;
import peer.Peer;
import peer.PeerConnection;
import messages.Handshake;
import messages.PeerMessage;

import java.util.ArrayList;

import static messages.PeerMessage.Type.*;

/**
 * The one who reaches out for handshakes
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
                Neighbor neighbor = State.getNeighborById(currId);

                neighbor.connection = new PeerConnection(neighbor.hostName, neighbor.port);
                Logger.logMakeConnection(State.us.id, currId);

                sendHandshake(neighbor);
                sendBitfield(neighbor);
                seeIfInterested(neighbor);

                (new Thread(() -> waitForMessages(neighbor))).start();
            } catch (Exception ignored){
                //not sure if we will need to handle this later
            }

        }
    }

    private void sendHandshake(Neighbor neighbor) {
        PeerConnection conn = neighbor.connection;

        // send a handshake
        conn.send(new Handshake(State.us.id).toByteArray());

        // read response
        byte[] res = new byte[32];
        conn.read(res, 32);

        // check if response is right
        if (new Handshake(res).equals(new Handshake(neighbor.id))) {
            System.out.println("Shook hands with " + neighbor.id);
        }
    }

    private void sendBitfield(Neighbor neighbor) {
        PeerConnection conn = neighbor.connection;

        conn.sendMessage(new PeerMessage(State.bitfieldSize, BITFIELD, State.us.bitField));

        PeerMessage res = conn.readMessage();
        State.getNeighborById(neighbor.id).bitField = res.payload;
    }

    protected void seeIfInterested(Neighbor neighbor) {

        ArrayList<Integer> newPieces = newPiecesFrom(neighbor.bitField);

        PeerMessage.Type interest = (newPieces.isEmpty())? NOT_INTERESTED : INTERESTED;
        neighbor.connection.sendMessage(new PeerMessage(0, interest, new byte[0]));
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

    protected void waitForMessages(Neighbor neighbor) {
        PeerConnection conn = neighbor.connection;
        // runs a loop check for if it receives a message, when it does so it will respond accordingly
        while (conn.getSocket().isConnected()) {
            PeerMessage msg = conn.readMessage();
            switch (msg.type){
                case CHOKE:
                    Logger.logChoke(State.us.id, neighbor.id);
                    break;
                case UNCHOKE:
                    Logger.logUnchoke(State.us.id, neighbor.id);
                    break;
                case INTERESTED:
                    Logger.logInterest(State.us.id, neighbor.id);
                    break;
                case NOT_INTERESTED:
                    Logger.logNotInterest(State.us.id, neighbor.id);
                    break;
                case HAVE:
                    Logger.logHave(State.us.id, neighbor.id);
                    // mark it down
                    break;
                case BITFIELD:
                    break;
                case REQUEST:
                    // check if they're choked/unchoked
                    // send a piece if they're unchoked
                    break;
                case PIECE:
                    // write the piece down
                    // send haves
                    // send not interested's
                    // send another request if still interested
                    break;
                default:
                    throw new RuntimeException("Invalid Message Type");
            }
        }
    }

}
