package talkers;

import peer.Neighbor;
import peer.PeerConnection;
import messages.Handshake;
import messages.PeerMessage;

import java.net.Socket;

import static messages.PeerMessage.Type.BITFIELD;

/**
 * The one who accepts and responds to handshakes
 */
public class PeerResponder extends PeerTalker implements Runnable {
    private Neighbor neighbor;
    private final PeerConnection conn;

    public PeerResponder(Socket socket)  {
        conn = new PeerConnection(socket);
    }

    @Override
    public void run() {
        receiveHandshake();
        receiveBitfield();
        seeIfInterested(neighbor);
        waitForMessages(neighbor);
    }

    private void receiveHandshake() {
        // read handshake
        byte[] buf = new byte[32];
        conn.read(buf, 32);
        Handshake handshake = new Handshake(buf);

        neighbor = State.getNeighborById(handshake.id);
        neighbor.connection = conn;

        // send back handshake
        conn.send(new Handshake(State.us.id).toByteArray());
        Logger.logConnectionEstablished(State.us.id, neighbor.id);
    }

    private void receiveBitfield() {
        // read bitfield
        PeerMessage res = conn.readMessage();
        State.getNeighborById(neighbor.id).bitField = res.payload;

        // send our bitfield
        conn.sendMessage(new PeerMessage(State.bitfieldSize, BITFIELD, State.us.bitField));
        System.out.println("Exchanged bitfields with " + neighbor.id);
    }

}