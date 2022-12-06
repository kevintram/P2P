package peer;

import messages.PeerMessage;

public class Neighbor extends Peer {
    public PeerConnection connection;
    public PeerMessage.Type ourInterestInThem;
    public PeerMessage.Type theirInterestInUs;
    public boolean canDown = false;
    public boolean isInit = false;
    public Neighbor(int id, String hostName, int port, boolean hasFile) {
        super(id, hostName, port, hasFile);
    }

    public void setOurInterestInThem(PeerMessage.Type newInterest) throws InterruptedException {
        if (ourInterestInThem == null) {
            this.connection.sendMessage(new PeerMessage(newInterest, new byte[0]));
        } else if (ourInterestInThem != newInterest) {
            this.connection.sendMessage(new PeerMessage(newInterest, new byte[0]));
        }
        ourInterestInThem = newInterest;
    }

}
