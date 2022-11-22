import peer.Neighbor;
import peer.Peer;
import talkers.*;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;

public class P2P {

    public static void main(String[] args) throws IOException {
        int id = Integer.parseInt(args[0]);

        State.path = "peer_" + id + File.separator;

        parsePeerInfoFile(id);
        parseCommonCfg();

        makePieces();
        State.us.startTime = getTime();
        //TODO make these run as separate threads
        startTalking();
        waitForPeersToTalkToMe();
    }

    private static void parsePeerInfoFile(int ourId) throws IOException {
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
                State.us = new Peer(id, hostName , port, hasFile);
                foundUs = true;
            } else {
                neighbors.add(new Neighbor(id, hostName, port, hasFile));
            }
        }

        State.startingId = neighbors.get(0).id;

        if (!foundUs) {
            throw new RuntimeException("Error: Could not find given id in PeerInfo.cfg!");
        }

        State.setNeighbors(neighbors);
    }

    private static void parseCommonCfg() throws IOException {
        File file = new File("Common.cfg");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String ss[];

        ss = br.readLine().split(" ");
        State.numPrefNeighbors = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        State.unchokeInterval = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        State.optimisticInterval = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        State.fileName = ss[1];

        ss = br.readLine().split(" ");
        State.fileSize = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        State.pieceSize = Integer.parseInt(ss[1]);
        State.numPieces = State.fileSize / State.pieceSize;
        State.finalPieceSize = State.fileSize % State.pieceSize;
        System.out.println(State.finalPieceSize);
        System.out.println(State.numPieces);
        State.bitfieldPaddingSize = (8 - (State.numPieces % 8));
        State.bitfieldSize = State.numPieces + State.bitfieldPaddingSize;

        // set our bitfield
        byte[] bitField = new byte[State.bitfieldSize];
        // fills array with 1's if it has file
        if (State.us.hasFile) {
            Arrays.fill(bitField, Integer.valueOf(1).byteValue());
            // padded bits need to be 0 always
            for (int i = 0; i < State.bitfieldPaddingSize; i++) {
                bitField[State.numPieces + i] = 0;
            }
        } else {
            Arrays.fill(bitField, Integer.valueOf(0).byteValue());
        }

        State.us.bitField = bitField;
    }

    public static void makePieces() throws IOException {
        String path = State.path;
        String fileName = State.fileName;
        Peer us = State.us;
        int numPieces = State.numPieces;
        Long startTime = us.startTime;
        Long currTime = getTime();
        us.downloadRate = (double)(numPieces) / (double)(startTime - currTime);

        for(int i = 0; i <= numPieces; i++) {
            PieceFileHelper.createPieceFile(path, i);
        }
        if(State.finalPieceSize > 0){
            PieceFileHelper.createPieceFile(path, State.numPieces + 1);
        }

        // if we have the file, write the file into the pieces
        if (us.hasFile) {
            File theFile = new File(path + File.separator + fileName);
            FileInputStream br = new FileInputStream(theFile);
            byte[] buff = new byte[State.pieceSize];

            for(int i = 0; i < State.numPieces; i++) {
                br.read(buff, 0, State.pieceSize);
                PieceFileHelper.updatePieceFile(path, i, buff);
            }

            if(State.finalPieceSize > 0){
                buff = new byte[State.finalPieceSize];
                br.read(buff, 0, State.finalPieceSize);
                PieceFileHelper.updatePieceFile(path, State.numPieces + 1, buff);
            }

        }

        // adds a shutdown hook, so on client termination, temp files will combine if the file is complete
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            boolean complete = true;
            for (int i = 1; i <= numPieces; i++){
                if(us.bitField[i - 1] == 0){
                    complete = false;
                    break;
                }
            }
            if (complete) {
                PieceFileHelper.combine(fileName, path);
               // Logger.logComplete(us.id);
            }
        }));
    }

    public static void waitForPeersToTalkToMe() {
        try {
            ServerSocket server = new ServerSocket(State.us.port);
            try {
                // when a peer tries to connect to us, run a talkers.PeerResponder
                while (true) {
                    new Thread(new PeerResponder(server.accept())).start();
                }
            } finally {
                server.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startTalking() {
        new PeerTalker().run();
    }

    //sorts by download rate, if 2 peers have same rate, 50/50 chance for order
    static class SortbyDownload implements Comparator<Peer> {
        public int compare(Peer a, Peer b){
            if(a.downloadRate == b.downloadRate){
                return new Random().nextInt(100) >= 50 ? -1 : 1;
            }
            return (int)(a.downloadRate - b.downloadRate);
        }
    }
    //idk a good name for this, clears the unchoked array, the recreates it from neighbor list
    //TODO diff version for if file complete
    public static void unchokeChoke(){
        for(Peer p : State.unchoked){
            //TODO send choke message
        }
        State.unchoked.clear();
        int left = State.numPrefNeighbors;
        Collections.sort(State.getNeighbors(), new SortbyDownload());
        State.unchoked = State.getNeighbors().subList(0, State.numPrefNeighbors);
        for(Peer p : State.unchoked){
            //TODO send unchoke message
        }
    }

    public static void optimChokeUnchoke(){
        int index = new Random().nextInt(State.getNeighbors().size() - State.numPrefNeighbors);
        index += State.numPrefNeighbors;
        boolean found = false;
        while(!found){
            if(State.getNeighbors().get(index).interested){
                State.optimisticNeighbor = State.getNeighbors().get(index);
            }
        }
    }

    public static Long getTime(){
        return System.nanoTime();
    }
}
