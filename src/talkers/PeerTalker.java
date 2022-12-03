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

    private int pending = -1;
    private boolean interested = false;

    public PeerTalker(Peer us, Neighbor nbr, PieceFileManager pfm, NeighborManager nm) {
        this.us = us;
        this.nbr = nbr;
        this.pfm = pfm;
        this.nm = nm;
    }

    @Override
    public void run() {
        try {
            start();
            while (!nm.haveAllConnections()) {} // wait until all connections are established
            updateAndSendInterest();
            waitForMessages();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void start() throws InterruptedException {
        Logger.logMakeConnection(us.id, nbr.id);
        nbr.connection = new PeerConnection(nbr.hostName, nbr.port);
        Logger.logConnectionEstablished(us.id, nbr.id);
        sendHandshake();
        //TODO figure out how we should actually handle this
        try {
            sendBitfield();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void sendHandshake() {
        // send a handshake
        nbr.connection.send(new Handshake(us.id).toByteArray());

        // read response
        byte[] res = new byte[32];
        nbr.connection.read(res, 32);

        // check if response is right
        if (!(new Handshake(res).equals(new Handshake(nbr.id)))) {
            throw new RuntimeException("Handshake with " + nbr.id + "did not match");
        }
    }

    private void sendBitfield() throws InterruptedException {
        nbr.connection.sendMessage(new PeerMessage(BITFIELD, us.getBitfield()));

        PeerMessage res = nbr.connection.readMessage();
        nbr.setBitfield(res.payload);
    }

    private void updateAndSendInterest() throws InterruptedException {
        boolean newInterested = getNewRandomPieceFrom(nbr.getBitfield()) != -1;
        if (interested != newInterested) {
            nbr.connection.sendMessage(new PeerMessage((newInterested)? INTERESTED : NOT_INTERESTED, new byte[0]));
        }
        interested = newInterested;
    }

    protected void sendRequest() throws InterruptedException {
        int piece;
        if (pending == -1) { // if nothing is pending
            piece = getNewRandomPieceFrom(nbr.getBitfield());
            if (piece == -1) {
                System.out.println("NO NEW PIECES FROM " + nbr.id + " EVEN THO WE'RE (" + us.id + ") INTERESTED");
                System.out.println("OUR (" + us.id +  ") BITFIELD: " + Arrays.toString(us.getBitfield()));
                System.out.println("THEIR(" + nbr.id +  ") BITFIELD: " + Arrays.toString(us.getBitfield()));
                System.exit(-1);
            }
            us.updateBitField(piece, (byte)-1, pfm.numPieces);
            pending = piece;
        } else {
            piece = pending;
        }

        System.out.println(us.id + " is requesting for " + piece + " from " + nbr.id);
        nbr.connection.sendMessage(new PeerMessage(REQUEST, Util.intToByteArr(piece)));
    }

    protected void waitForMessages() throws InterruptedException {
        PeerConnection conn = nbr.connection;
        // runs a loop check for if it receives a message, when it does so it will respond accordingly
        while (conn.getSocket().isConnected()) {
            PeerMessage msg = conn.readMessage();

            if (msg == null) { // we just leave if we read bad
                leaveIfEverybodyDone();
                System.out.println("SOMEONE LEFT BUT " + us.id + " IS NOT DONE");
                System.out.println(us.id + " MY BITFIELD IS " + Arrays.toString(us.bitfield));
                System.exit(0);
            }

            switch (msg.type){
                case CHOKE:
                    Logger.logChoke(us.id, nbr.id);
                    if (pending != -1) {
                        us.updateBitField(pending, (byte)0, pfm.numPieces);
                        pending = -1;
                    }
                    break;
                case UNCHOKE:
                    Logger.logUnchoke(us.id, nbr.id);
                    updateAndSendInterest();
                    if (interested) {
                        sendRequest();
                    }
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
                    nbr.updateBitField(i, (byte)1, pfm.numPieces);
                    leaveIfEverybodyDone();

                    updateAndSendInterest();
                    if (interested) {
                        sendRequest();
                    }

                    break;
                case BITFIELD:
                    throw new RuntimeException("ERROR!:" + us.id + " received a bitfield message from " + nbr.id + " (we shouldn't have)");
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

    private void respondToRequestMsg(PeerMessage msg) throws InterruptedException {
        int pieceIndex = Util.byteArrToInt(msg.payload);
        System.out.println(us.id + " got request for " + pieceIndex + " from " + nbr.id);
        byte[] piece = pfm.getByteArrOfPiece(pieceIndex);

        if (nm.unchoked.contains(nbr) || nbr == nm.optimNbr) { // only give them a piece if they're unchoked
            int payloadLen = 4 + piece.length;
            byte[] payload = new byte[payloadLen];
            System.arraycopy(msg.payload, 0, payload, 0, 4); // write index into payload
            System.arraycopy(piece, 0, payload, 4, piece.length); // write piece into payload
            nbr.downloadRate++;
            nbr.connection.sendMessage(new PeerMessage(PIECE, payload));
        } else {
            System.out.println("BUT FUCK YOU " + nbr.id + "! BITCH YOU CHOKED!");
        }
    }

    private void respondToPieceMsg(PeerMessage msg) throws InterruptedException {
        // write the piece down
        byte[] indexBuf = Arrays.copyOfRange(msg.payload, 0, 4);
        int index = Util.byteArrToInt(indexBuf);

        byte[] pieceContent = Arrays.copyOfRange(msg.payload, 4, msg.len);

        if (us.getBitfield()[index] != 1) { // if we don't have this piece yet
            pfm.updatePieceFile(index, pieceContent);
            us.updateBitField(index, (byte)1, pfm.numPieces);

            pending = -1;

            Logger.logDownload(us.id, nbr.id, index);
            // send haves to neighbors
            for (Neighbor n : nm.getNeighbors()) {
                n.connection.sendMessage(new PeerMessage(HAVE, Util.intToByteArr(index)));
            }

            if(us.hasFile) {
                Logger.logComplete(us.id);
                leaveIfEverybodyDone();
            }
        }

        updateAndSendInterest();
        if (interested) {
            sendRequest();
        }
    }

    /**
     * @return the index of a random new piece (one that we don't have). Returns -1 if they have nothing new.
     */
    private int getNewRandomPieceFrom(byte[] bitfield) throws InterruptedException {
        ArrayList<Integer> indices = new ArrayList<>();

        for(int i = 0; i < us.getBitfield().length && i < bitfield.length; i++) {
            if(us.getBitfield()[i] == 0 && bitfield[i] == 1){
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

    private void leaveIfEverybodyDone() {
        if (nm.allNeighborsDone() && us.hasFile) {
            System.out.println(us.id + " IS FUCKING DONE AND LEAVING");
            System.exit(0);
        }
    }

}
