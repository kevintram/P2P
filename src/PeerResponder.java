import messages.BitField;
import messages.Handshake;
import messages.PeerMessage;
import messages.Util;

import java.net.Socket;
import java.util.ArrayList;

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
        buf = new byte[peer.pieceCount];
        conn.read(buf, 5 + (peer.pieceCount));
        //indecies of what we have that they dont, will try to send off as many of these as we can, wait for interest after each send
        ArrayList<Integer>haves = BitField.hasNew(buf, peer.getBitField(), peer.pieceCount);
        state.getPeerById(handshake.id).makeBitfield(buf);
        // send bitfield
        //TODO imma be honest, idk if this works but we'll see ig
        PeerMessage msg = new PeerMessage(peer.pieceCount, PeerMessage.Type.BITFIELD, peer.getBitField());
        conn.send(msg.payload);
        //runs a loop check for if it recieves a message, when it does so it will respond accordingly
        int size;
        int type;
        while (conn.getSocket().isConnected()) {
            try{
                //set for getting message length
                buf = new byte[4];
                conn.read(buf, 4);
                size = Util.byteArrToInt(buf);
                //buffer is now size to read message, plus one byte to get message type
                buf = new byte[size + 1];
                //gets message type
                conn.read(buf, 1);
                type = Util.byteArrToInt(buf);
                switch (type){
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                    default:
                        throw new RuntimeException("Invalid Message Type");
                }
            }catch (RuntimeException e){
                //this currently does nothing but might later depending on how we want to deal with this
                continue;
            }

        }

    }

}