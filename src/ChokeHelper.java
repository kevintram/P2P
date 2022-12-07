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
        synchronized (nm) {

            if (nm.noNeighborsInterestedInUs()) return; // don't do anything if nobody is interested in us

            ArrayList<Neighbor> newUnchoked = new ArrayList<>();

            if (!us.hasFile) {
                List<Neighbor> downloadList = new ArrayList<>(); // neighbors we're interested in aka ones we're downloading from
                for (Neighbor n : nm.getNeighbors()) {
                    if (n.isInit && n.ourInterestInThem == PeerMessage.Type.INTERESTED) downloadList.add(n);
                }

                // add top k neighbors to unchoked
                downloadList.sort((a, b) -> (int) (a.downloadRate - b.downloadRate)); // sort by download rate
                newUnchoked.addAll(downloadList.subList(0, Math.min(downloadList.size(), numPrefNeighbors)));


            } else {
                //picks random neighbors to unchoke
                for (int i = 0; i < numPrefNeighbors; i++) {
                    Neighbor randNeighbor = getRandomNeighbor();
                    // keep on picking a random neighbor until you get one that already isn't unchoked
                    while (randNeighbor == nm.optimisticNeighbor || newUnchoked.contains(randNeighbor) || !randNeighbor.isInit) {
                        randNeighbor = getRandomNeighbor();
                    }
                    newUnchoked.add(randNeighbor);
                }
            }
            // reset all download rates
            for (Neighbor nbr : nm.getNeighbors()) {
                nbr.downloadRate = 0;
            }
            Logger.logChangeNeighbors(us.id, newUnchoked);

            for (Neighbor n : newUnchoked) {
                if (!nm.unchoked.contains(n) && nm.optimisticNeighbor != n) { // send unchokes to neighbors newly unchoked
                    n.connection.sendMessage(new PeerMessage(PeerMessage.Type.UNCHOKE, new byte[0]));
                }
            }

            for (Neighbor n : nm.unchoked) {
                if (!newUnchoked.contains(n) && nm.optimisticNeighbor != n) { // send chokes to neighbors not choked anymore
                    n.connection.sendMessage(new PeerMessage(PeerMessage.Type.CHOKE, new byte[0]));
                }
            }

            nm.unchoked.clear();
            nm.unchoked.addAll(newUnchoked);
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
        synchronized (nm) {
            if (nm.noNeighborsInterestedInUs()) return; // don't do anything if nobody is interested in us

            if (nm.getNeighbors().size() <= nm.unchoked.size()) {
                return;
            }

            Neighbor newOptimNbr = getRandomNeighbor();
            while (!newOptimNbr.isInit || nm.unchoked.contains(newOptimNbr)) {
                newOptimNbr = getRandomNeighbor();
            }

            if (newOptimNbr != nm.optimisticNeighbor) { // if the neighbor changed
                if (!nm.unchoked.contains(newOptimNbr)) { // send an unchoke if new not already unchoked
                    newOptimNbr.connection.sendMessage(new PeerMessage(PeerMessage.Type.UNCHOKE, new byte[0]));
                }

                if (!nm.unchoked.contains(nm.optimisticNeighbor) && nm.optimisticNeighbor != null) {  // send a choke if old is now choked
                    nm.optimisticNeighbor.connection.sendMessage(new PeerMessage(PeerMessage.Type.CHOKE, new byte[0]));
                }
            }

            nm.optimisticNeighbor = newOptimNbr;
            Logger.logOptChangeNeighbor(us.id, nm.optimisticNeighbor.id);
        }
    }

    private Neighbor getRandomNeighbor() {
        return nm.getNeighbors().get(new Random().nextInt(nm.getNeighbors().size()));
    }

    public static Long getTime() {
        return System.nanoTime();
    }
}

