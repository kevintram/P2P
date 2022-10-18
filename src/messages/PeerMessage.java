package messages;

public class PeerMessage {
    public int len;
    public Type type;
    public byte[] payload;

    // parse an array of bytes to construct a message
    PeerMessage(int len, byte[] b) {

    }

    public PeerMessage(int len, Type type, byte[] payload) {

    }

    byte[] toByteArray() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        } else {
            PeerMessage other = (PeerMessage) obj;
            if(other.len == this.len &&
                other.type == this.type) {
                for(int i = 0; i < this.len; i++){
                    if(this.payload[i] != other.payload[i]){
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    }

    public enum Type {
        CHOKE, UNCHOKE,
        INTERESTED, NOT_INTERESTED,
        HAVE, BITFIELD,
        REQUEST, PIECE
    }
}


