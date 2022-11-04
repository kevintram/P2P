package talkers;

import messages.Util;
import peer.Neighbor;
import peer.Peer;
import peer.PeerConnection;
import messages.Handshake;
import messages.PeerMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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

        PeerMessage.Type interest = (hasSomethingNew(neighbor.bitField))? INTERESTED: NOT_INTERESTED;
        neighbor.connection.sendMessage(new PeerMessage(0, interest, new byte[0]));

        if (interest == INTERESTED) {
            int i = randomPieceFrom(neighbor.bitField);
            System.out.println("Requesting for " + i);
            neighbor.connection.sendMessage(new PeerMessage(4, REQUEST, Util.intToByteArr(i)));
        }
    }

    /**
     * @param them bitfield of peer we're looking at
     * @return the index of a random new piece (one that we don't have). Returns -1 if they have nothing new.
     */
    protected int randomPieceFrom(byte[] them){
        ArrayList<Integer> indices = new ArrayList<>();
        byte[] us = State.us.bitField;

        for(int i = 0; i < State.bitfieldSize; i++) {
            if(them[i] == 1 && us[i] == 0){
                indices.add(i);
            }
        }

        if (indices.isEmpty()) {
            return -1;
        } else {
            Random random = new Random();
            return indices.get(random.nextInt(indices.size()));
        }
    }

    /**
     * @param them bitfield of peer we're looking at
     * @return returns true if they have something new. Returns false if they don't.
     */
    protected boolean hasSomethingNew(byte[] them) {
        byte[] us = State.us.bitField;

        for (int i = 0; i < State.bitfieldSize; i++) {
            if(them[i] == 1 && us[i] == 0){
                return true;
            }
        }

        return false;
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
                    // for now, just gonna pretend nobody is choked
                    int pieceIndex = Util.byteArrToInt(msg.payload);
                    System.out.println("Got request for " + pieceIndex);

                    byte[] piece = PieceFileHelper.getByteArrOfPiece(State.path, pieceIndex);

                    int payloadLen = 4 + piece.length;
                    byte[] payload = new byte[payloadLen];
                    System.arraycopy(msg.payload, 0, payload, 0, 4); // write index into payload
                    System.arraycopy(piece, 0, payload, 4, piece.length); // write piece into payload

                    neighbor.connection.sendMessage(new PeerMessage(payloadLen, PIECE, payload));
                    break;
                case PIECE:
                    // write the piece down
                    byte[] indexBuf = Arrays.copyOfRange(msg.payload, 0, 4);
                    int index = Util.byteArrToInt(indexBuf);

                    byte[] pieceContent = Arrays.copyOfRange(msg.payload, 4, msg.len);

                    PieceFileHelper.updatePieceFile(State.path, index, pieceContent);
                    State.us.bitField[index] = 1;

                    Logger.logDownload(State.us.id, neighbor.id, index);

                    // TODO: send haves and not-interesteds
//                    for (Neighbor n : State.getNeighbors()) {
//                        n.connection.sendMessage(new PeerMessage(4, HAVE, Util.intToByteArr(index)));
//                    }

                    // send another request if still interested
                    byte[] us = State.us.bitField;
                    if (hasSomethingNew(neighbor.bitField)) {
                        int i = randomPieceFrom(neighbor.bitField);
                        neighbor.connection.sendMessage(new PeerMessage(4, REQUEST, Util.intToByteArr(i)));
                    }
                    break;
                default:
                    throw new RuntimeException("Invalid Message Type");
            }
        }
    }

}
