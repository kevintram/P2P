import peer.Neighbor;
import peer.Peer;
import talkers.PeerResponder;
import talkers.PeerTalker;
import talkers.PieceFileHelper;
import talkers.State;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;

public class P2P {

    public static void main(String[] args) throws IOException {
        int id = Integer.parseInt(args[0]);

        State.path = "peer_" + id + File.separator;

        parsePeerInfoFile(id);
        parseCommonCfg();

        makePieces();

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
        State.bitfieldPaddingSize = (8 - (State.numPieces % 8));
        State.bitfieldSize = State.numPieces + State.bitfieldPaddingSize;

        // set our bitfield
        byte[] bitField = new byte[State.bitfieldSize];
        // fills array with 1's if it has file
        if (State.us.hasFile) {
            Arrays.fill(bitField, Integer.valueOf(1).byteValue());
            // padded bits need to be 0 always
            for (int i = 0; i <= State.bitfieldPaddingSize; i++) {
                bitField[State.numPieces + i - 1] = 0;
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

        for(int i = 0; i < numPieces; i++) {
            PieceFileHelper.createPieceFile(path, i);
        }

        // if we have the file, write the file into the pieces
        if (us.hasFile) {
            File theFile = new File(path + File.separator + fileName);
            FileInputStream br = new FileInputStream(theFile);
            byte[] buff = new byte[8];

            for(int i = 0; i < numPieces; i++) {
                br.read(buff, 0, 8);
                PieceFileHelper.updatePieceFile(path, i, buff);
            }
        }

        // adds a shutdown hook, so on client termination, temp files will combine if the file is complete
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            boolean complete = true;
            for (byte b : us.bitField){
                if(b == 0){
                    complete = false;
                    break;
                }
            }
            if (complete) {
                PieceFileHelper.combine(numPieces, fileName, path);
            }
        }));
    }

    public static void waitForPeersToTalkToMe() {
        try {
            ServerSocket server = new ServerSocket(State.us.port);
            try {
                // when a peer tries to connect to us, run a talkers.PeerResponder
                while (true) {
                    new PeerResponder(server.accept()).run();
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
}
