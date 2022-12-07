package talkers;

import logger.Logger;
import messages.Handshake;
import messages.PeerMessage;
import neighbor.NeighborManager;
import peer.Peer;
import peer.PeerConnection;
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
    public void run() {
        synchronized (conn) {
            super.run();
        }
    }

    @Override
    protected void init() throws InterruptedException, IOException {
        receiveHandshake();
        receiveBitfield();
    }

    private void receiveHandshake() throws IOException, InterruptedException {
        // read handshake
        byte[] buf = new byte[32];
        conn.read(buf, 32);
        Handshake handshake = new Handshake(buf);
        nm.getNeighborById(handshake.id).connection = conn;
        nbr = nm.getNeighborById(handshake.id);
        nbr.connection.getSocket().setReceiveBufferSize(pfm.normalPieceSize + 10);
        // send back handshake
        conn.send(new Handshake(us.id).toByteArray());
        Logger.logConnectionEstablished(us.id, nbr.id);
    }

    private void receiveBitfield() throws InterruptedException {
        // read bitfield
        PeerMessage bitfieldMsg = conn.readMessage();
        nbr.setBitfield(bitfieldMsg.payload);

        if (bitfieldMsg.type != BITFIELD) {
            System.out.println("WE SHOULD'VE GOTTEN A BITFIELD!! BUT WE GOT A " + bitfieldMsg.type);
            System.exit(-1);
        }

        // send our bitfield
        conn.sendMessage(new PeerMessage(BITFIELD, us.getBitfield()));
    }
}