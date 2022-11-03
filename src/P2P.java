import Peer.Peer;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;

public class P2P {

    public static void main(String[] args) throws IOException {
        int id = Integer.parseInt(args[0]);

        ProcessState state = new ProcessState();

        state.path = "peer_" + id + File.separator;

        parsePeerInfoFile(state, id);
        parseCommonCfg(state);

        makePieces(state);

        //TODO make these run as separate threads
        startTalking(state);
        waitForPeersToTalkToMe(state);
    }

    private static void parsePeerInfoFile(ProcessState state, int ourId) throws IOException {
        ArrayList<Peer> peers = new ArrayList<Peer>();

        File file = new File("PeerInfo.cfg");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String lineBuf;
        while ((lineBuf = br.readLine()) != null){
            String[] lineSplit = lineBuf.split(" ");

            int id = Integer.parseInt(lineSplit[0]);
            String hostName = lineSplit[1];
            int port = Integer.parseInt(lineSplit[2]);
            boolean hasFile = Integer.parseInt(lineSplit[3]) == 1;

            peers.add(new Peer(id, hostName , port, hasFile));
        }

        state.startingId = peers.get(0).id;

        state.setPeers(peers);

        boolean foundUs = false;
        for (Peer p : peers) {
            if (p.id == ourId) {
                state.us = p;
                foundUs = true;
                break;
            }
        }

        if (!foundUs) {
            throw new RuntimeException("Error: Could not find given id in PeerInfo.cfg!");
        }
    }

    private static void parseCommonCfg(ProcessState state) throws IOException {
        File file = new File("Common.cfg");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String ss[];

        ss = br.readLine().split(" ");
        state.numPrefNeighbors = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        state.unchokeInterval = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        state.optimisticInterval = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        state.fileName = ss[1];

        ss = br.readLine().split(" ");
        state.fileSize = Integer.parseInt(ss[1]);

        ss = br.readLine().split(" ");
        state.pieceSize = Integer.parseInt(ss[1]);

        state.numPieces = state.fileSize / state.pieceSize;
        state.bitfieldPaddingSize = (8 - (state.numPieces % 8));
        state.bitfieldSize = state.numPieces + state.bitfieldPaddingSize;

        // set our bitfield
        byte[] bitField = new byte[state.bitfieldSize];
        // fills array with 1's if it has file
        if (state.us.hasFile) {
            Arrays.fill(bitField, Integer.valueOf(1).byteValue());
            // padded bits need to be 0 always
            for (int i = 0; i <= state.bitfieldPaddingSize; i++) {
                bitField[state.numPieces + i - 1] = 0;
            }
        } else {
            Arrays.fill(bitField, Integer.valueOf(0).byteValue());
        }

        state.us.bitField = bitField;
    }

    public static void makePieces(ProcessState state) throws IOException {
        String path = state.path;
        String fileName = state.fileName;
        Peer us = state.us;
        int numPieces = state.numPieces;

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

    public static void waitForPeersToTalkToMe(ProcessState state) {
        try {
            ServerSocket server = new ServerSocket(state.us.port);
            try {
                // when a peer tries to connect to us, run a PeerResponder
                while (true) {
                    new PeerResponder(server.accept(), state).run();
                }
            } finally {
                server.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startTalking(ProcessState state) {
        new PeerTalker(state).run();
    }
}
