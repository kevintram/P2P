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

import java.io.IOException;
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
        try {
            init();
            nbr.isInit = true;
            waitForMessages();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void init() throws InterruptedException, IOException {
        nbr.connection = new PeerConnection(nbr.hostName, nbr.port);
        nbr.connection.getSocket().setReceiveBufferSize(pfm.normalPieceSize + 10);
        Logger.logMakeConnection(us.id, nbr.id);
        sendHandshake();
        sendBitfield();
    }

    private void sendHandshake() throws InterruptedException {
        // send a handshake
        nbr.connection.send(new Handshake(us.id).toByteArray());

        // read response
        byte[] res = new byte[32];
        nbr.connection.read(res, 32);
    }

    private void sendBitfield() throws InterruptedException {
        nbr.connection.sendMessage(new PeerMessage(BITFIELD, us.getBitfield()));

        PeerMessage bitfieldMsg = nbr.connection.readMessage();

        if (bitfieldMsg.type != BITFIELD) {
            System.out.println("WE SHOULD'VE GOTTEN A BITFIELD!! BUT WE GOT A " + bitfieldMsg.type);
            System.exit(-1);
        }

        nbr.setBitfield(bitfieldMsg.payload);
        for(int i = 0; i < nbr.getBitfield().length; i++){
            if(us.getBitfield()[i] == 0 && nbr.getBitfield()[i] == 1){
                nbr.setOurInterestInThem(INTERESTED);
                return;
            }
        }
        nbr.setOurInterestInThem(NOT_INTERESTED);
    }

    private int checkInterest() throws InterruptedException {
        int i = getNewRandomPieceFrom(nbr.getBitfield());
        if(i != -1) nbr.setOurInterestInThem(INTERESTED);
        else nbr.setOurInterestInThem(NOT_INTERESTED);
        return i;
    }

    protected void requestForPiecesIfInterested() throws InterruptedException {
        int i = checkInterest();
        if (nbr.ourInterestInThem == INTERESTED) {
            System.out.println(us.id + "->" + nbr.id + " trying updating our bitfield");
            us.pendingBitfield(i);
            System.out.println(us.id + "->" + nbr.id + " updated our bitfield successfully!");

            System.out.println(us.id + "->" + nbr.id + " sending a request");
            nbr.connection.sendMessage(new PeerMessage(REQUEST, Util.intToByteArr(i)));
        }
    }

    protected void waitForMessages() throws InterruptedException, IOException {

        PeerConnection conn = nbr.connection;
        // runs a loop check for if it receives a message, when it does so it will respond accordingly
        while (conn.getSocket().isConnected()) {
            System.out.println(us.id + "->" + nbr.id + " waiting for a message...");
            PeerMessage msg = conn.readMessage();

            if (msg == null) {
                System.exit(0);
            }
            System.out.println(us.id + "->" + nbr.id + " got a " + msg.type.name() + " message");

            new Thread(() -> {
                try {
                    respond(msg);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void respond(PeerMessage msg) throws IOException, InterruptedException {
        synchronized (nbr) {
            synchronized (nm) {
                switch (msg.type) {
                    case CHOKE:
                        //if choke, I cant download, dont request
                        nbr.canDown = false;
                        Logger.logChoke(us.id, nbr.id);
                        break;
                    case UNCHOKE:
                        //if unchoke, I can download
                        nbr.canDown = true;
                        Logger.logUnchoke(us.id, nbr.id);
                        requestForPiecesIfInterested();
                        break;
                    case INTERESTED:
                        nbr.theirInterestInUs = INTERESTED;
                        Logger.logInterest(us.id, nbr.id);
                        break;
                    case NOT_INTERESTED:
                        nbr.theirInterestInUs = NOT_INTERESTED;
                        Logger.logNotInterest(us.id, nbr.id);
                        break;
                    case HAVE:
                        Logger.logHave(us.id, nbr.id);
                        int i = Util.byteArrToInt(msg.payload);
                        nbr.updateBitfield(i, pfm.numPieces);

                        leaveIfEverybodyDone();

                        if (us.getBitfield()[i] == 0) {
                            nbr.setOurInterestInThem(INTERESTED);
                        }

                        if (nbr.canDown) {
                            requestForPiecesIfInterested();
                        }
                        break;
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
    }

    private void respondToRequestMsg(PeerMessage msg) throws InterruptedException {
        int pieceIndex = Util.byteArrToInt(msg.payload);
        byte[] piece = pfm.getByteArrOfPiece(pieceIndex);

        int payloadLen = 4 + piece.length;
        byte[] payload = new byte[payloadLen];

        System.arraycopy(piece, 0, payload, 4, piece.length); // write piece into our payload
        System.arraycopy(msg.payload, 0, payload, 0, 4); // write index into our payload

        //System.out.println("Type: " + PIECE + " index: " + pieceIndex + " Piece: " + new String(piece));

        nbr.connection.sendMessage(new PeerMessage(PIECE, payload));
    }

    private void respondToPieceMsg(PeerMessage msg) throws InterruptedException, IOException {
        System.out.println(us.id + "->" + nbr.id + " got a piece");
        // write the piece down
        byte[] indexBuf = Arrays.copyOfRange(msg.payload, 0, 4);
        int index = Util.byteArrToInt(indexBuf);

        byte[] pieceContent = Arrays.copyOfRange(msg.payload, 4, msg.len);

        pfm.updatePieceFile(index, pieceContent);
        us.updateBitfield(index, pfm.numPieces);

        Logger.logDownload(us.id, nbr.id, index);
        nbr.downloadRate++;

        // send haves to neighbors
        System.out.println(us.id + "->" + nbr.id + " sending haves to neighbors");
        for (Neighbor n : nm.getNeighbors()) {
            System.out.println(us.id + " sending have to " + nbr.id);
            if(n.isInit) n.connection.sendMessage(new PeerMessage(HAVE, Util.intToByteArr(index)));
            System.out.println(us.id + " sent have to " + nbr.id);
        }
        System.out.println(us.id + "->" + nbr.id + " finished sending haves to neighbors");

        if (nbr.canDown) {
            System.out.println(us.id + "->" + nbr.id + "can download because unchoked");
            requestForPiecesIfInterested();
        } else {
            System.out.println(us.id + "->" + nbr.id + "can't download because choked");
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
            System.out.println(us.id + " has finished, starting exit...");
            System.exit(0);
        }
    }

}
