package talkers;

import logger.Logger;
import neighbor.NeighborManager;
import peer.Peer;
import peer.PeerConnection;
import messages.Handshake;
import messages.PeerMessage;
import piece.PieceFileManager;

import java.io.IOException;
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
        receiveHandshake();
        receiveBitfield();
    }

    private void receiveHandshake() {
        // read handshake
        byte[] buf = new byte[32];
        conn.read(buf, 32);
        Handshake handshake = new Handshake(buf);
        nm.getNeighborById(handshake.id).connection = conn;
        nm.getNeighborById(handshake.id).isInit = true;
        nbr = nm.getNeighborById(handshake.id);

        // send back handshake
        conn.send(new Handshake(us.id).toByteArray());
        Logger.logConnectionEstablished(us.id, nbr.id);


    }

    private void receiveBitfield() throws InterruptedException {
        // read bitfield
        PeerMessage res = conn.readMessage();
        nbr.setBitfield(res.payload);

        // send our bitfield
        conn.sendMessage(new PeerMessage(BITFIELD, us.getBitfield()));
        System.out.println("Exchanged bitfields with " + nbr.id);
    }
}