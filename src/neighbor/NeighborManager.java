package neighbor;

import peer.Neighbor;

import java.util.*;

public class NeighborManager {
    private final HashMap<Integer, Neighbor> idToNeighbor = new HashMap<>();
    public List<Neighbor> unchoked = new ArrayList<>();
    public Neighbor optimNbr;
    public int unchokeInterval;
    public int optimInterval;
    public int numPrefNeighbors;

    public NeighborManager(List<Neighbor> neighbors) {
        for (Neighbor n: neighbors) {
            idToNeighbor.put(n.id, n);
        }
    }

    public Neighbor getNeighborById(int id) {
        return idToNeighbor.get(id);
    }

    public List<Neighbor> getNeighbors() {
        return new ArrayList<>(idToNeighbor.values());
    }

    public boolean allNeighborsDone() {
        for (Neighbor n : idToNeighbor.values()) {
            if (!n.hasFile) {
                return false;
            }

        }
        System.out.println();
        return true;
    }

    public boolean haveAllConnections() {
        for (Neighbor n : idToNeighbor.values()) {
            if (n.connection == null) {
                return false;
            }
        }
        return true;
    }
}
