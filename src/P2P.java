import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;

public class P2P {
    public static void main(String[] args) {
        int id = Integer.parseInt(args[0]);

        List<Peer> config = parseConfigFile();
        Peer us = tryToFindUs(id, config);

        PeerState state = new PeerState(us, config);

        PeerTalker process = new PeerTalker();
        process.state = state;

        process.run();
        runServer(state);
    }

    public static List<Peer> parseConfigFile() {
        return Arrays.asList(
                new Peer(1001, "localhost", 6001, true),
                new Peer(1002, "localhost", 6002, false),
                new Peer(1003, "localhost", 6003, false),
                new Peer(1004, "localhost", 6004, false),
                new Peer(1005, "localhost", 6005, false),
                new Peer(1006, "localhost", 6006, false)
        );
    }

    public static Peer tryToFindUs(int id, List<Peer> config) {
        Peer us = null;
        boolean foundUs = false;

        for (Peer p : config) {
            if (p.id == id) {
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
}
