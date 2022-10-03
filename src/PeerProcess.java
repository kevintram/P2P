import java.util.Arrays;
import java.util.List;

public class PeerProcess {
    public static void main(String[] args) {
        int peerID = Integer.parseInt(args[0]);
        List<PeerInfo> peerInfo = Arrays.asList(
                new PeerInfo(1001, "localhost", 6001, true),
                new PeerInfo(1002, "localhost", 6002, false),
                new PeerInfo(1003, "localhost", 6003, false),
                new PeerInfo(1004, "localhost", 6004, false),
                new PeerInfo(1005, "localhost", 6005, false),
                new PeerInfo(1006, "localhost", 6006, false)
        );

        int port = 0;

        for (PeerInfo p : peerInfo) {
            if (p.peerID == peerID) {
                port = p.port;
                break;
            }
        }

        Peer peer = new Peer(peerID, port, peerInfo);
        peer.run();
    }
}
