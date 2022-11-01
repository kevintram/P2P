import messages.BitField;
import messages.PeerMessage;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class Peer {
    public final int id;
    public final String hostName;
    public final int port;
    public boolean hasFile;
    public PeerConnection connection;

    private byte bitField[];

    public int prefNeigh;
    public int unchokeInterval;
    public int optemisticInterval;
    public String fileName;
    public int fileSize;
    public int pieceSize;
    public int pieceCount;
    //TODO set this path
    private String path;

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
        //if you have the file, need to generate all temp files
        File complete = new File(String.format("%s\\%s",path,fileName));
        for(int i = 0; i < pieceCount; i+= 8){
            if(hasFile){
                FileInputStream br = new FileInputStream(complete);
                byte[] buff = new byte[8];
                br.read(buff, i, 8);
                //if it doesnt work, will try to delete file and create again

                if(!FileHandler.createPieceFile(path, i, Optional.of(buff))){
                    File temp = new File(String.format("%s\\%i.tmp", path, i));
                    temp.delete();
                    if(!FileHandler.createPieceFile(path, i, Optional.of(buff))){
                        throw new IOException("Failed to make piece file: " + i);
                    }
                }
            } else {
                if(!FileHandler.createPieceFile(path, i, Optional.empty())){
                    File temp = new File(String.format("%s\\%i.tmp", path, i));
                    temp.delete();
                    if(!FileHandler.createPieceFile(path, i, Optional.empty())){
                        throw new IOException("Failed to make piece file: " + i);
                    }
                }
            }

        }
        //adds a shutdown hook, so on client termination, temp files will combine if the file is complete
        this.shutdown();
    }

    private void shutdown(){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            boolean complete = true;
            for(byte b : bitField){
                if(b == 0){
                    complete = false;
                    break;
                }
            }
            if(complete){
                FileHandler.combine(pieceCount, fileName, path);
            }
        }));
    }


    //updates the bitfield so that it keeps up with pieces being downloaded
    public boolean updateBitField(int index) {
        try {
            this.bitField[index] = 1;
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("could not update bitfield, piece out of bounds");
            return false;
        }
        boolean complete = true;
        for(byte b : bitField){
            if(b == 0){
                complete = false;
                continue;
            }
        }
        if(complete) this.hasFile = true;
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
    //ensures if the bitfield is retrieved, it will have the right number of bits
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
    //find what we have that they don't, return have messages to send for all of those
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