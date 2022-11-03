import Peer.Peer;

import java.util.*;

/**
 * Holds global state about this process.
 * THIS SHOULD HAVE LIKE NO FUNCTIONALITY IT JUST HOLDS SHIT
 * THIS SHOULD BE A SINGLETON!!!
 */
public class ProcessState {
    private final HashMap<Integer, Peer> idToPeer;
    public final ArrayList<Peer> unchoked;

    public Peer us;

    public int startingId;

    public int numPrefNeighbors;
    public int unchokeInterval;
    public int optimisticInterval;

    public String fileName;
    public int fileSize;

    public int pieceSize;
    public int numPieces;
    public int bitfieldSize;
    public int bitfieldPaddingSize; // if number of pieces isn't divisible by 8, need to pad with 0's

    public String path;

    public ProcessState() {
        idToPeer = new HashMap<>();
        unchoked = new ArrayList<>();
    }

    public void setPeers(List<Peer> peers) {
        for (Peer p: peers) {
            idToPeer.put(p.id, p);
        }
    }

    public Peer getPeerById(int id) {
        return idToPeer.get(id);
    }
}
