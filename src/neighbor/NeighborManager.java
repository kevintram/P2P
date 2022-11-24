package neighbor;

import peer.Neighbor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NeighborManager {
    private final HashMap<Integer, Neighbor> idToNeighbor = new HashMap<>();
    public List<Neighbor> unchoked = new ArrayList<>();
    public Neighbor optimisticNeighbor;

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
}
