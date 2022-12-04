package peer;

import messages.PeerMessage;

public class Neighbor extends Peer {
    public PeerConnection connection;
    public PeerMessage.Type interested;
    public PeerMessage.Type theyInterest;
    public boolean canDown = false;
    public int downlaodRate = 0;
    public boolean isInit = false;
    public Neighbor(int id, String hostName, int port, boolean hasFile) {
        super(id, hostName, port, hasFile);
    }

    public void setInterested(PeerMessage.Type newInterest) throws InterruptedException {
        if (interested == null) {
            this.connection.sendMessage(new PeerMessage(newInterest, new byte[0]));
        } else if (interested != newInterest) {
            this.connection.sendMessage(new PeerMessage(newInterest, new byte[0]));
        }
        interested = newInterest;
    }

}
