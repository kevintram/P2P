import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class P2P {
    public static void main(String[] args) throws IOException {
        int id = Integer.parseInt(args[0]);

        List<Peer> peers = parsePeerInfo();
        Peer us = tryToFindUs(id, peers);

        PeerState state = new PeerState(us, peers);

        //TODO make these run as separate threads
        startTalking(state);
        runServer(state);
    }
    public static List<Peer> parsePeerInfo() throws IOException {
        ArrayList<Peer> peers = new ArrayList<>();
        //TODO probably works
        File file = new File(".\\config\\PeerInfo.cfg");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String ss[];
        String s;
        String hostname;
        int port;
        boolean hasFile;
        while ((s = br.readLine()) != null){
            ss = s.split(" ");
            peers.add(new Peer(Integer.parseInt(ss[0]), ss[1], Integer.parseInt(ss[2]), (Integer.parseInt(ss[3]) == 1)));
        }
        return peers;
    }

    public static Peer tryToFindUs(int ourId, List<Peer> config) {
        Peer us = null;
        boolean foundUs = false;

        for (Peer p : config) {
            if (p.id == ourId) {
                us = p;
                foundUs = true;
                break;
            }
        }

        if (!foundUs) {
            throw new RuntimeException("Error: Could not find given id in config file!");
        }

        return us;
    }

    public static void runServer(PeerState state) {
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

    public static void startTalking(PeerState state) {
        new PeerTalker(state).run();
    }
}
