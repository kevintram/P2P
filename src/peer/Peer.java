package peer;

public class Peer {
    public final int id;
    public final String hostName;
    public final int port;
    public boolean hasFile;
    public byte[] bitField;

    public Peer(int id, String hostName, int port, boolean hasFile) {
        this.id = id;
        this.hostName = hostName;
        this.port = port;
        this.hasFile = hasFile;
    }
}