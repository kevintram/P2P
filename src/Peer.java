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

    public byte bitField[];

    public int prefNeigh;
    public int unchokeInterval;
    public int optemisticInterval;
    public String fileName;
    public int fileSize;
    public int pieceSize;

    boolean hasPiece = false;


    public Peer(int id, String hostName, int port, boolean hasFile) throws IOException {
        this.id = id;
        this.hostName = hostName;
        this.port = port;
        this.hasFile = hasFile;
        parseCommonCfg();
        this.bitField = new byte[fileSize/pieceSize];
        if(hasFile) {
            Arrays.fill(bitField, Integer.valueOf(1).byteValue());
            hasPiece = true;
        }
    }
    //idk where this makes more sense, but since all peers follow it will stay const in state
    private void parseCommonCfg() throws IOException {
        //TODO fix path to work locally
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
    }

}
