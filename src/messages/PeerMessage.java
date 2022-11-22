package messages;
import java.util.Arrays;
import java.util.Optional;

public class PeerMessage {
    public int len;
    public Type type;
    public byte[] payload;

    // parse an array of bytes to construct a message
    public PeerMessage(byte[] b) {
        len = Util.byteArrToInt(Arrays.copyOfRange(b, 0, 4)); // length in first 4 bytes
        type = Type.values()[b[5]]; // type in 5th byte
        payload = Arrays.copyOfRange(b, 6, 6 + len);
    }

    public PeerMessage(int len, Type type, Optional<byte[]> payload) {
        this.len = len;
        this.type = type;
        if(payload.isPresent()){
            this.payload = payload.get();
        }
    }

    public byte[] toByteArray() {
        byte[] res = new byte[5 + len];

        System.arraycopy(Util.intToByteArr(len), 0, res, 0, 4); // write length
        res[4] = (byte) type.ordinal();
        System.arraycopy(payload, 0, res, 5, len); // write payload

        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == this.getClass()) {
            PeerMessage other = (PeerMessage) obj;

            boolean lengthMatches = other.len == this.len;
            boolean typeMatches = other.type == this.type;
            boolean payloadMatches = Arrays.equals(other.payload, this.payload);

            return lengthMatches && typeMatches && payloadMatches;
        }
        return false;
    }

    public enum Type {
        CHOKE, UNCHOKE,
        INTERESTED, NOT_INTERESTED,
        HAVE, BITFIELD,
        REQUEST, PIECE
    }
}


