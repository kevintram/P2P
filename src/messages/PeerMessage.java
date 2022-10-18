package messages;

public class PeerMessage {
    public int len;
    public Type type;
    public byte[] payload;

    // parse an array of bytes to construct a message
    PeerMessage(int len, byte[] b) {

    }

    PeerMessage(int len, Type type, byte[] payload) {

    }

    byte[] toByteArray() {
        return null;
    }

    enum Type {
        CHOKE, UNCHOKE,
        INTERESTED, NOT_INTERESTED,
        HAVE, BITFIELD,
        REQUEST, PIECE
    }
}


