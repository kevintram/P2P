import neighbor.NeighborManager;
import peer.Neighbor;
import peer.Peer;
import piece.PieceFileManager;
import talkers.*;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;

public class P2P {
    static PieceFileManager pfm;
    static NeighborManager nm;
    static Peer us;
    static ChokeHelper ch;

    public static void main(String[] args) throws IOException, InterruptedException {
        int id = Integer.parseInt(args[0]);
        System.out.format("Peer: %d \n", id);
        initStuffFromPeerInfoCfg(id);
        initStuffFromCommonCfg();
        startTalkingTo(nm.getNeighbors());
        startChokingThreads(nm.unchokeInterval, nm.optimInterval);
        waitForPeersToTalkToMe();
    }

    /**
     * Sets us and our neighbors from the PeerInfo.cfg file
     * @param ourId
     * @throws IOException
     */
    private static void initStuffFromPeerInfoCfg(int ourId) throws IOException {
        ArrayList<Neighbor> neighbors = new ArrayList<>();

        boolean foundUs = false;
        File file = new File("PeerInfo.cfg");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String lineBuf;
        while ((lineBuf = br.readLine()) != null){
            String[] lineSplit = lineBuf.split(" ");
            int id = Integer.parseInt(lineSplit[0]);

            String hostName = lineSplit[1];
            int port = Integer.parseInt(lineSplit[2]);
            boolean hasFile = Integer.parseInt(lineSplit[3]) == 1;

            if (id == ourId) {
                us = new Peer(id, hostName , port, hasFile);
                foundUs = true;
            } else {
                neighbors.add(new Neighbor(id, hostName, port, hasFile));
            }
        }

        if (!foundUs) {
            throw new RuntimeException("Error: Could not find given id in PeerInfo.cfg!");
        }

        nm = new NeighborManager(neighbors);
    }

    private static void startChokingThreads(int unchokeInterval, int optimisticInterval) {
        ch = new ChokeHelper(nm, us, optimisticInterval, unchokeInterval);
    }

    /**
     * Sets all the attributes related to Common.cfg
     * @throws IOException
     */
    private static void initStuffFromCommonCfg() throws IOException, InterruptedException {
        File file = new File("Common.cfg");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String ss[];

        ss = br.readLine().split(" ");
        int numPrefNeighbors = Integer.parseInt(ss[1]);
        nm.numPrefNeighbors = numPrefNeighbors;
        ss = br.readLine().split(" ");
        int unchokeInterval = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        int optimisticInterval = Integer.parseInt(ss[1]);

        nm.optimInterval = optimisticInterval;
        nm.unchokeInterval = unchokeInterval;

        ss = br.readLine().split(" ");
        String fileName = ss[1];

        ss = br.readLine().split(" ");
        int fileSize = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        int pieceSize = Integer.parseInt(ss[1]);

        int numPieces =  (int)Math.ceil((double)fileSize / pieceSize);
        int finalPieceSize = fileSize % pieceSize;
        int bitfieldPaddingSize = (8 - (numPieces % 8));
        int bitfieldSize = numPieces + bitfieldPaddingSize;

        String ourPath = "peer_" + us.id + File.separator;
        pfm = new PieceFileManager(ourPath, fileName, pieceSize, finalPieceSize, numPieces);

        // set our bitfield
        byte[] bitField = new byte[bitfieldSize];
        // fills array with 1's if it has file
        if (us.hasFile) {
            Arrays.fill(bitField, Integer.valueOf(1).byteValue());
            // padded bits need to be 0 always
            for (int i = 0; i < bitfieldPaddingSize; i++) {
                bitField[numPieces + i] = 0;
            }
        } else {
            Arrays.fill(bitField, Integer.valueOf(0).byteValue());
        }

        us.setBitfield(bitField);
        pfm.makePieces(us);
    }

    public static void startTalkingTo(List<Neighbor> neighbors) {
            for (Neighbor neighbor : neighbors) {
                if (neighbor.id < us.id) {
                    new Thread(new PeerTalker(us, neighbor, pfm, nm), neighbor.id + " Talker for " + us.id).start();
                }
            }
    }

    public static void waitForPeersToTalkToMe() {
        try {
            ServerSocket server = new ServerSocket(us.port);
            try {
                // when a peer tries to connect to us, run a talkers.PeerResponder
                while (true) {
                    new Thread(new PeerResponder(server.accept(), us, pfm, nm), us.id + " Responder for" + nm.getNeighbors().size()).start();
                }
            } finally {
                server.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}