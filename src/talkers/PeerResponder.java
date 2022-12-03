package talkers;

import logger.Logger;
import neighbor.NeighborManager;
import peer.Peer;
import peer.PeerConnection;
import messages.Handshake;
import messages.PeerMessage;
import piece.PieceFileManager;

import java.net.Socket;

import static messages.PeerMessage.Type.BITFIELD;

/**
 * The one who accepts and responds to handshakes
 */
public class PeerResponder extends PeerTalker {
    private final PeerConnection conn;

    public PeerResponder(Socket socket, Peer us, PieceFileManager pfm, NeighborManager nm)  {
        super(us, null, pfm, nm);
        conn = new PeerConnection(socket);
    }

    @Override
    protected void start() throws InterruptedException {
        receiveAndSendHandshake();
        receiveAndSendBitfield();
    }

    private void receiveAndSendHandshake() {
        // read handshake
        byte[] buf = new byte[32];
        conn.read(buf, 32);
        Handshake handshake = new Handshake(buf);

        nbr = nm.getNeighborById(handshake.id);
        nbr.connection = conn;

        // send back handshake
        conn.send(new Handshake(us.id).toByteArray());
        Logger.logConnectionEstablished(us.id, nbr.id);
    }

    private void receiveAndSendBitfield() throws InterruptedException {
        // read bitfield
        PeerMessage msg = conn.readMessage();
        nbr.setBitfield(msg.payload);

        // send our bitfield
        conn.sendMessage(new PeerMessage(BITFIELD, us.getBitfield()));
    }
}