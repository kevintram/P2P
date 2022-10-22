import messages.BitField;
import messages.PeerMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Peer {
    public final int id;
    public final String hostName;
    public final int port;
    public final boolean hasFile;
    public PeerConnection connection;

    private byte bitField[];

    public int prefNeigh;
    public int unchokeInterval;
    public int optemisticInterval;
    public String fileName;
    public int fileSize;
    public int pieceSize;
    public int pieceCount;


    public Peer(int id, String hostName, int port, boolean hasFile) throws IOException {

        this.id = id;
        this.hostName = hostName;
        this.port = port;
        this.hasFile = hasFile;
        parseCommonCfg();
        //if the number of pieces isnt divisible by 8, needs trailing 0's
        this.bitField = new byte[fileSize/pieceSize + (8 - ((fileSize/pieceSize) % 8))];
        if(hasFile) {
            //fills array with 1's if it has file
            Arrays.fill(bitField, Integer.valueOf(1).byteValue());
        } else {
            Arrays.fill(bitField, Integer.valueOf(0).byteValue());
        }
        for(int i = 0; i < (8 - ((fileSize/pieceSize) % 8)); i++){
            //minus one here should offset counting at 0, but may cause wipe of last piece
            bitField[(fileSize/pieceSize + i) - 1] = 0;
        }
    }

    //updates the bitfield so that it keeps up with pieces being downloaded
    public boolean updateBitField(int index) {

        try {
            this.bitField[index] = 1;
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("could not update bitfield, piece out of bounds");
            return false;
        }
        return true;
    }
    //idk where this makes more sense, but since all peers follow it will stay const in state
    private void parseCommonCfg() throws IOException {

        File file = new File("C:\\Users\\lackt\\Documents\\UF\\Fall 2022\\CNT4007\\P2P\\config\\common.cfg");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String ss[];
        ss = br.readLine().split(" ");
        this.prefNeigh = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        this.unchokeInterval = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        this.optemisticInterval = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        this.fileName = ss[1];

        ss = br.readLine().split(" ");
        this.fileSize = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        this.pieceSize = Integer.parseInt(ss[1]);

        this.pieceCount = fileSize/pieceSize;
    }
    //ensures if the bitfield is retrieved, it will have right number of bits
    public void makeBitfield(byte[] buf) {

        int len = this.fileSize/this.pieceSize;
        int overflow = 8 - (len % 8);
        try {
            for(int i = 0; i < len; i++) {
                //buffer is full bitfield msg, so skip len and type (5 bytes)
                this.bitField[i] = buf[4+i];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("bitfield retrieved wrong length");
        }
    }
    //find what we have that they dont, return have messages to send for all of those
    public ArrayList<PeerMessage> sendHaves(PeerMessage bitfieldPacket) {

        ArrayList<PeerMessage> haveMsgs = new ArrayList<>();
        ArrayList<Integer> indices = BitField.doesntHave(this.bitField, bitfieldPacket.payload, fileSize/pieceSize);
        for(int i : indices){
            //TODO make have messages
            //once theyre made, will need to be sent one at a time, and await a interested or not
        }
        return haveMsgs;
    }

    public final byte[] getBitField() {

        return bitField;
    }
}