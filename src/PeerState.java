import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * holds global state about us
 */
// TODO: idk if this should just be a part of peer process but I feel like this will be get pretty big as we go on
public class PeerState {

    public final Peer us;
    private final HashMap<Integer, Peer> idToPeer;
    public final ArrayList<Peer> unchoked;

    public PeerState(Peer us, List<Peer> config) {

        this.us = us;
        idToPeer = new HashMap<>();
        unchoked = new ArrayList<>();

        for (Peer p : config) {
            idToPeer.put(p.id, p);
        }
    }

    public Peer getPeerById(int id) {
        return idToPeer.get(id);
    }
}
