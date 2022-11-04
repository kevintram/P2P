package talkers;

import peer.Neighbor;
import peer.Peer;

import java.util.*;

/**
 * Holds global state about this process.
 * THIS SHOULD HAVE LIKE NO FUNCTIONALITY IT JUST HOLDS SHIT
 */
public class State {
    private static final HashMap<Integer, Neighbor> idToNeighbor = new HashMap<>();
    public static final ArrayList<Neighbor> unchoked = new ArrayList<>();

    public static Peer us;

    public static int startingId;

    public static int numPrefNeighbors;
    public static int unchokeInterval;
    public static int optimisticInterval;

    public static String fileName;
    public static int fileSize;

    public static int pieceSize;
    public static int numPieces;
    public static int bitfieldPaddingSize; // if number of pieces isn't divisible by 8, need to pad with 0's
    public static int bitfieldSize; // this should = numPieces + bitfieldPaddingSize

    public static String path;

    public static void setNeighbors(List<Neighbor> peers) {
        for (Neighbor p: peers) {
            idToNeighbor.put(p.id, p);
        }
    }

    public static Neighbor getNeighborById(int id) {
        return idToNeighbor.get(id);
    }

    public static Collection<Neighbor> getNeighbors() {
        return idToNeighbor.values();
    }
}
