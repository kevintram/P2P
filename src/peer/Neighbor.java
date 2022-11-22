package peer;

public class Neighbor extends Peer {
    public PeerConnection connection;
    public boolean interested;
    public Neighbor(int id, String hostName, int port, boolean hasFile) {
        super(id, hostName, port, hasFile);
    }

//    //updates the bitfield so that it keeps up with pieces being downloaded
//    public boolean updateBitField(int index) {
//        try {
//            this.bitField[index] = 1;
//        } catch (ArrayIndexOutOfBoundsException e){
//            System.out.println("could not update bitfield, piece out of bounds");
//            return false;
//        }
//        boolean complete = true;
//        for (byte b : bitField){
//            if (b == 0){
//                complete = false;
//                break;
//            }
//        }
//        if (complete) hasFile = true;
//        return true;
//    }
//
//    //ensures if the bitfield is retrieved, it will have the right number of bits
//    public void setBitfield(byte[] buf, int len) {
//        int overflow = 8 - (len % 8);
//        try {
//            //start at 4 since buffer is full bitfield msg, so skip len and type (5 bytes)
//            System.arraycopy(buf, 4, this.bitField, 0, len);
//        } catch (IndexOutOfBoundsException e) {
//            System.out.println("bitfield retrieved wrong length");
//        }
//    }
}
