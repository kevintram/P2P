import messages.BitField;
import messages.Handshake;

import java.net.Socket;

/**
 * Reads and responds to a peer's messages
 */
// TODO: idk if this should extend PeerProcess but I feel like they would have a lot of overlap in functionality
public class PeerResponder extends PeerTalker implements Runnable {
    private Peer peer;
    private final PeerConnection conn;

    public PeerResponder(Socket socket, PeerState state)  {
        super(state);
        conn = new PeerConnection(socket);
    }

    @Override
    public void run() {
        // read handshake
        byte[] buf = new byte[32];
        conn.read(buf, 32);
        Handshake handshake = new Handshake(buf);

        peer = state.getPeerById(handshake.id);
        peer.connection = conn;
        // send back handshake
        conn.send(new Handshake(state.us.id).toByteArray());
        System.out.println("Shook hands with " + peer.id);
        // read bitfield

        // send bitfield
        //TODO imma be honest, idk if this works but we'll see ig
        BitField bitField = new BitField(peer.bitField, peer.fileSize/ peer.pieceSize);
        conn.send(bitField.bitfield);
        // read messages and respond accordingly
    }
}