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
        PeerMessage res = conn.readMessage(State.bitfieldSize);
        State.getPeerById(peer.id).bitField = res.payload;

        // send our bitfield
        conn.sendMessage(new PeerMessage(State.bitfieldSize, BITFIELD, State.us.bitField));
        System.out.println("Exchanged bitfields with " + peer.id);
    }

//    private void waitForMessages() {
//        //runs a loop check for if it receives a message, when it does so it will respond accordingly
//        while (conn.getSocket().isConnected()) {
//            //set for getting message length
//            byte[] buf = new byte[4];
//            conn.read(buf, 4);
//            int size = Util.byteArrToInt(buf);
//            //buffer is now size to read message, plus one byte to get message type
//            buf = new byte[size + 1];
//            //gets message type
//            conn.read(buf, 1);
//            int type = Util.byteArrToInt(buf);
//            switch (type){
//                case 0:
//                    break;
//                case 1:
//                    break;
//                case 2:
//                    break;
//                case 3:
//                    break;
//                case 4:
//                    break;
//                case 5:
//                    break;
//                case 6:
//                    break;
//                case 7:
//                    break;
//                default:
//                    throw new RuntimeException("Invalid Message Type");
//            }
//        }
//    }

}