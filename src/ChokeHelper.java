import logger.Logger;
import messages.PeerMessage;
import neighbor.NeighborManager;
import peer.Neighbor;
import peer.Peer;

import java.io.IOException;
import java.util.*;

public class ChokeHelper {
    Peer us;
    public int numPrefNeighbors;
    NeighborManager nm;

    List<Neighbor> downloadList = new ArrayList<>();

    public ChokeHelper(NeighborManager nm, Peer us, int optimisticInterval, int unchokeInterval) {
        Timer time = new Timer();
        this.nm = nm;
        this.us = us;
        this.numPrefNeighbors = nm.numPrefNeighbors;
        ChokeUnchokeTask ct = new ChokeUnchokeTask();
        OptimUnChoke ot = new OptimUnChoke();
        time.schedule(ct, 0, unchokeInterval * 1000);
        time.schedule(ot, 0, optimisticInterval * 1000);
    }

    public class ChokeUnchokeTask extends TimerTask {
        @Override
        public void run() {
            try {
                unchokeChoke();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    //idk a good name for this, clears the unchoked array, the recreates it from neighbor list
    public void unchokeChoke() throws InterruptedException, IOException {

        if (nm.noNeighborsInterestedInUs()) return; // don't do anything if nobody is interested in us

        nm.unchoked.clear();

        if (!us.hasFile) {
            //sorts neighbors by download rate, and picks the top n
            for (Neighbor n : nm.getNeighbors()) {
                if (n.isInit && n.theirInterestInUs == PeerMessage.Type.INTERESTED) downloadList.add(n);
            }

            // add top k neighbors to unchoked
            downloadList.sort((a, b) -> (int) (a.downloadRate - b.downloadRate)); // sort by download rate
            nm.unchoked.addAll(downloadList.subList(0, Math.min(downloadList.size(), numPrefNeighbors)));

            // reset all download rates
            for (Neighbor nbr : nm.getNeighbors()) {
                nbr.downloadRate = 0;
            }
        } else {
            //picks random neighbors to unchoke
            for (int i = 0; i < numPrefNeighbors; i++) {
                Neighbor randNeighbor = getRandomNeighbor();
                // keep on picking a random neighbor until you get one that already isn't unchoked
                while (nm.unchoked.contains(randNeighbor) || !randNeighbor.isInit) {
                    randNeighbor = getRandomNeighbor();
                }
                nm.unchoked.add(randNeighbor);
            }
        }

        Logger.logChangeNeighbors(us.id, nm.unchoked);

        for (Neighbor p : nm.unchoked) {
            p.connection.sendMessage(new PeerMessage(PeerMessage.Type.UNCHOKE, new byte[0]));
        }

    }

    public class OptimUnChoke extends TimerTask {
        @Override
        public void run() {
            try {
                optimChokeUnchoke();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void optimChokeUnchoke() throws InterruptedException, IOException {
        if (nm.optimisticNeighbor != null) {
            nm.optimisticNeighbor.connection.sendMessage(new PeerMessage(PeerMessage.Type.CHOKE, new byte[0]));
        }

        if (nm.noNeighborsInterestedInUs()) return; // don't do anything if nobody is interested in us
        if (nm.getNeighbors().size() <= nm.unchoked.size()) {
            return;
        }

        Neighbor randNeighbor = getRandomNeighbor();
        while (!randNeighbor.isInit) {
            randNeighbor = getRandomNeighbor();
        }
        nm.optimisticNeighbor = randNeighbor;

        Logger.logOptChangeNeighbor(us.id, nm.optimisticNeighbor.id);
        nm.optimisticNeighbor.connection.sendMessage(new PeerMessage(PeerMessage.Type.UNCHOKE, new byte[0]));
    }

    private Neighbor getRandomNeighbor() {
        return nm.getNeighbors().get(new Random().nextInt(nm.getNeighbors().size()));
    }

    public static Long getTime() {
        return System.nanoTime();
    }
}

