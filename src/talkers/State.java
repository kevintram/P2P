package talkers;

import peer.Peer;

import java.util.*;

/**
 * Holds global state about this process.
 * THIS SHOULD HAVE LIKE NO FUNCTIONALITY IT JUST HOLDS SHIT
 */
public class State {
    private static final HashMap<Integer, Peer> idToPeer = new HashMap<>();
    public static final ArrayList<Peer> unchoked = new ArrayList<>();

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

    public static void setPeers(List<Peer> peers) {
        for (Peer p: peers) {
            idToPeer.put(p.id, p);
        }
    }

    public static Peer getPeerById(int id) {
        return idToPeer.get(id);
    }
}
