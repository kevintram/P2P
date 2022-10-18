import messages.BitField;
import messages.Handshake;
import messages.PeerMessage;

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
        //needs to resize buffer to handle bitfield
        buf = new byte[peer.fileSize/ peer.pieceSize];
        conn.read(buf, 5 + (peer.fileSize / peer.pieceSize));
        state.getPeerById(handshake.id).makeBitfield(buf);

                // send bitfield
        //TODO imma be honest, idk if this works but we'll see ig
        PeerMessage msg = new PeerMessage(peer.fileSize/ peer.pieceSize, PeerMessage.Type.BITFIELD, peer.bitField);
        conn.send(msg.payload);
        // read messages and respond accordingly
    }


}