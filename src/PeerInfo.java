public class PeerInfo {
    public int peerID;
    public String hostName;
    public int port;
    public boolean hasFile;

    public PeerInfo(int peerID, String hostName, int port, boolean hasFile) {
        this.peerID = peerID;
        this.hostName = hostName;
        this.port = port;
        this.hasFile = hasFile;
    }
}
