package peer;

public class Neighbor extends Peer {
    public PeerConnection connection;
    public Neighbor(int id, String hostName, int port, boolean hasFile) {
        super(id, hostName, port, hasFile);
    }

}
