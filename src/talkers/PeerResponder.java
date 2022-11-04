package talkers;

import peer.Peer;
import peer.PeerConnection;
import messages.Handshake;
import messages.PeerMessage;

import java.net.Socket;

import static messages.PeerMessage.Type.BITFIELD;

/**
 * Reads and responds to a peer's messages
 */
// TODO: idk if this should extend PeerProcess but I feel like they would have a lot of overlap in functionality
public class PeerResponder extends PeerTalker implements Runnable {
    private Peer peer;
    private final PeerConnection conn;

    public PeerResponder(Socket socket)  {
        conn = new PeerConnection(socket);
    }

    @Override
    public void run() {
        receiveHandshake();
        receiveBitfield();
        seeIfInterested(conn, peer.id);
//        waitForMessages();
    }

    private void receiveHandshake() {
        // read handshake
        byte[] buf = new byte[32];
        conn.read(buf, 32);
        Handshake handshake = new Handshake(buf);

        peer = State.getPeerById(handshake.id);
        peer.connection = conn;

        // send back handshake
        conn.send(new Handshake(State.us.id).toByteArray());
        Logger.logConnectionEstablished(State.us.id, peer.id);
    }

    private void receiveBitfield() {
        // read bitfield
        PeerMessage res = conn.readMessage();
        State.getPeerById(peer.id).bitField = res.payload;

        // send our bitfield
        conn.sendMessage(new PeerMessage(State.bitfieldSize, BITFIELD, State.us.bitField));
        System.out.println("Exchanged bitfields with " + peer.id);
    }

    private void waitForMessages() {
        // runs a loop check for if it receives a message, when it does so it will respond accordingly
        while (conn.getSocket().isConnected()) {
            PeerMessage msg = conn.readMessage();
            switch (msg.type){
                case CHOKE:
                    Logger.logChoke(State.us.id, peer.id);
                    break;
                case UNCHOKE:
                    Logger.logUnchoke(State.us.id, peer.id);
                    break;
                case INTERESTED:
                    Logger.logInterest(State.us.id, peer.id);
                    break;
                case NOT_INTERESTED:
                    Logger.logNotInterest(State.us.id, peer.id);
                    break;
                case HAVE:
                    Logger.logHave(State.us.id, peer.id);
                    break;
                case BITFIELD:
                    break;
                case REQUEST:
                    break;
                case PIECE:
                    break;
                default:
                    throw new RuntimeException("Invalid Message Type");
            }
        }
    }

}