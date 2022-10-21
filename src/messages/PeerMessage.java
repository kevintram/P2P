package messages;
import java.util.Arrays;
public class PeerMessage {
    public int len;
    public Type type;
    public byte[] payload;

    // parse an array of bytes to construct a message
    PeerMessage(byte[] b) {
        // find length using byte array to int converter in Util
        this.len = Util.byteArrToInt(b);
        // assign type to variable for switch statement for use w/ enums
        int payloadType = b[5];
        // switch statement to assign enum types to this.type
        switch(payloadType) {
            case 0:
                this.type = Type.CHOKE;
                break;
            case 1:
                this.type = Type.UNCHOKE;
                break;
            case 2:
                this.type = Type.INTERESTED;
                break;
            case 3:
                this.type = Type.NOT_INTERESTED;
                break;
            case 4:
                this.type = Type.HAVE;
                break;
            case 5:
                this.type = Type.BITFIELD;
                break;
            case 6:
                this.type = Type.REQUEST;
                break;
            case 7:
                this.type = Type.PIECE;
                break;
        }
        // take all elements past element 5 and assign it to be this.payload
        this.payload = Arrays.copyOfRange(payload, 5, payload.length);
    }

    public PeerMessage(int len, Type type, byte[] payload) {
        // assign length
        this.len = len;
        // assign type
        this.type = type;
        // assign payload
        this.payload = payload;
    }

    byte[] toByteArray() {
        // allocate 5 bytes for length and type, and this.len bytes for the payload
        byte[] arrOut = new byte[5 + this.len];
        // get the byte array of the payload length
        byte[] lengthOfPayload = Util.intToByteArr(this.len);
        // assign each value in the payload length byte array to be in the output byte array
        for(int i = 0; i < 4; i++){
            arrOut[i] = lengthOfPayload[i];
        }
        // switch based on type to assign the 5th element in the array
        switch(this.type) {
            case CHOKE:
                arrOut[4] = 0;
                break;
            case UNCHOKE:
                arrOut[4] = 1;
                break;
            case INTERESTED:
                arrOut[4] = 2;
                break;
            case NOT_INTERESTED:
                arrOut[4] = 3;
                break;
            case HAVE:
                arrOut[4] = 4;
                break;
            case BITFIELD:
                arrOut[4] = 5;
                break;
            case REQUEST:
                arrOut[4] = 6;
                break;
            case PIECE:
                arrOut[4] = 7;
                break;
        }
        // assign each byte in the payload to an element in the output byte array
        for(int i = 0; i < this.len; i++){
            arrOut[5 + i] = this.payload[i];
        }
        // return output byte array
        return arrOut;
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


