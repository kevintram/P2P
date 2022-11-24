package talkers;

import logger.Logger;
import messages.Util;
import neighbor.NeighborManager;
import peer.Neighbor;
import peer.Peer;
import peer.PeerConnection;
import messages.Handshake;
import messages.PeerMessage;
import piece.PieceFileManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static messages.PeerMessage.Type.*;

/**
 * The one who reaches out for handshakes
 */
public class PeerTalker implements Runnable {

    protected Peer us;
    protected Neighbor nbr;
    protected PieceFileManager pfm;
    protected NeighborManager nm;

    public PeerTalker(Peer us, Neighbor nbr, PieceFileManager pfm, NeighborManager nm) {
        this.us = us;
        this.nbr = nbr;
        this.pfm = pfm;
        this.nm = nm;
    }

    @Override
    public void run() {
        nbr.connection = new PeerConnection(nbr.hostName, nbr.port);
        Logger.logMakeConnection(us.id, nbr.id);

        sendHandshake();
        sendBitfield();
        requestForPiecesIfInterested();
        waitForMessages();
    }

    private void sendHandshake() {
        // send a handshake
        nbr.connection.send(new Handshake(us.id).toByteArray());

        // read response
        byte[] res = new byte[32];
        nbr.connection.read(res, 32);

        // check if response is right
        if (new Handshake(res).equals(new Handshake(nbr.id))) {
            System.out.println("Shook hands with " + nbr.id);
        }
    }

    private void sendBitfield() {

        nbr.connection.sendMessage(new PeerMessage(BITFIELD, us.bitfield));

        PeerMessage res = nbr.connection.readMessage();
        nbr.bitfield = res.payload;
    }

    protected void requestForPiecesIfInterested() {
        int i = getNewRandomPieceFrom(nbr.bitfield);

        PeerMessage.Type interest = (i != -1)? INTERESTED: NOT_INTERESTED;
        nbr.connection.sendMessage(new PeerMessage(interest, new byte[0]));

        if (interest == INTERESTED) {
            System.out.println("Requesting for " + i);
            nbr.connection.sendMessage(new PeerMessage(REQUEST, Util.intToByteArr(i)));
        }
    }

    protected void waitForMessages() {
        PeerConnection conn = nbr.connection;
        // runs a loop check for if it receives a message, when it does so it will respond accordingly
        while (conn.getSocket().isConnected()) {
            PeerMessage msg = conn.readMessage();
            switch (msg.type){
                case CHOKE:
                    Logger.logChoke(us.id, nbr.id);
                    break;
                case UNCHOKE:
                    Logger.logUnchoke(us.id, nbr.id);
                    break;
                case INTERESTED:
                    Logger.logInterest(us.id, nbr.id);
                    break;
                case NOT_INTERESTED:
                    Logger.logNotInterest(us.id, nbr.id);
                    break;
                case HAVE:
                    Logger.logHave(us.id, nbr.id);
                    int i = Util.byteArrToInt(msg.payload);
                    nbr.bitfield[i] = 1;
                case BITFIELD:
                    throw new RuntimeException("Received a bitfield message from " + nbr.id + " (we shouldn't have)");
                case REQUEST:
                    respondToRequestMsg(msg);
                    break;
                case PIECE:
                    respondToPieceMsg(msg);
                    break;
                default:
                    throw new RuntimeException("Invalid Message Type");
            }
        }
    }

    private void respondToRequestMsg(PeerMessage msg) {
        // for now, just gonna pretend nobody is choked
        int pieceIndex = Util.byteArrToInt(msg.payload);
        System.out.println("Got request for " + pieceIndex + " from " + nbr.id);
        byte[] piece = pfm.getByteArrOfPiece(pieceIndex);

        int payloadLen = 4 + piece.length;
        byte[] payload = new byte[payloadLen];
        System.arraycopy(msg.payload, 0, payload, 0, 4); // write index into payload
        System.arraycopy(piece, 0, payload, 4, piece.length); // write piece into payload

        nbr.connection.sendMessage(new PeerMessage(PIECE, payload));
    }

    private void respondToPieceMsg(PeerMessage msg) {
        // write the piece down
        byte[] indexBuf = Arrays.copyOfRange(msg.payload, 0, 4);
        int index = Util.byteArrToInt(indexBuf);

        byte[] pieceContent = Arrays.copyOfRange(msg.payload, 4, msg.len);

        pfm.updatePieceFile(index, pieceContent);
        us.bitfield[index] = 1;

        Logger.logDownload(us.id, nbr.id, index);

        // send haves to neighbors
        for (Neighbor n : nm.getNeighbors()) {
            n.connection.sendMessage(new PeerMessage(HAVE, Util.intToByteArr(index)));
        }

        // send another request if still interested
        int i = getNewRandomPieceFrom(nbr.bitfield);
        if (i > -1)
            nbr.connection.sendMessage(new PeerMessage(REQUEST, Util.intToByteArr(i)));
    }

    /**
     * @return the index of a random new piece (one that we don't have). Returns -1 if they have nothing new.
     */
    private int getNewRandomPieceFrom(byte[] bitfield) {
        ArrayList<Integer> indices = new ArrayList<>();

        for(int i = 0; i < us.bitfield.length && i < bitfield.length; i++) {
            if(us.bitfield[i] == 0 && bitfield[i] == 1){
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

}
