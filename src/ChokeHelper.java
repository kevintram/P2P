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
    public int unchokeInterval;
    public int optimisticInterval;
    NeighborManager nm;

    List<Neighbor> downloadList = new ArrayList<>();

    public ChokeHelper(NeighborManager nm, Peer us, int optimisticInterval, int unchokeInterval) {
        Timer time = new Timer();
        this.nm = nm;
        this.us = us;
        this.numPrefNeighbors = nm.numPrefNeighbors;
        ChokeUnchokeTask ct = new ChokeUnchokeTask();
        OptimeChokeTask ot = new OptimeChokeTask();
        time.schedule(ct, 0, unchokeInterval * 1000);
        time.schedule(ot, 0, optimisticInterval * 1000);
    }

    //sorts by download rate, if 2 peers have same rate, 50/50 chance for order
    static class SortbyDownload implements Comparator<Peer> {
        public int compare(Peer a, Peer b) {
            if (a.downloadRate == b.downloadRate) {
                return new Random().nextInt(100) >= 50 ? -1 : 1;
            }
            return (int) (a.downloadRate - b.downloadRate);
        }
    }

    public class ChokeUnchokeTask extends TimerTask {

        @Override
        public void run() {
            try {
                unchokeChoke();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //idk a good name for this, clears the unchoked array, the recreates it from neighbor list
    public void unchokeChoke() throws InterruptedException, IOException {
        //us.updateDownloadRate(P2P.pfm.numPieces);

        boolean cont = false;
        for (Neighbor p : P2P.nm.unchoked) {

            p.connection.sendMessage(new PeerMessage(PeerMessage.Type.CHOKE, new byte[0]));

        }
        for (Neighbor n : nm.getNeighbors()) {
            if (n.theyInterest == PeerMessage.Type.INTERESTED) {
                cont = true;
            }
        }
        nm.unchoked.clear();
        if (!cont) return;
        if (!us.hasFile) {
            //sorts neighbors by download rate, and picks the top n
            for (Neighbor n : nm.getNeighbors()) {
                if (n.isInit && n.theyInterest == PeerMessage.Type.INTERESTED)
                    downloadList.add(n);
            }
            Collections.sort(downloadList, new SortbyDownload());
            if (downloadList.size() > numPrefNeighbors) {
                nm.unchoked.addAll(downloadList.subList(0, numPrefNeighbors));
            } else {
                nm.unchoked.addAll(downloadList);
            }
            Logger.logChangeNeighbors(us.id, nm.unchoked);
            for (Neighbor nbr : nm.getNeighbors()) {
                nbr.downloadRate = 0;
            }
        } else {
            //picks random neighbors to unchoke
            List<Integer> index = new ArrayList<>();
            for (int i = 0; i < numPrefNeighbors; i++) {
                int prefIndex = new Random().nextInt(nm.getNeighbors().size());
                while (!nm.getNeighbors().get(prefIndex).isInit ||  index.contains(prefIndex)) {
                    prefIndex = new Random().nextInt(nm.getNeighbors().size());
                }
                index.add(prefIndex);
            }
            for (int i : index) {
                nm.unchoked.add(nm.getNeighbors().get(i));
            }
            Logger.logChangeNeighbors(us.id, nm.unchoked);
        }
        for (Neighbor p : nm.unchoked) {
            p.connection.sendMessage(new PeerMessage(PeerMessage.Type.UNCHOKE, new byte[0]));
        }

    }

    public class OptimeChokeTask extends TimerTask {
        @Override
        public void run() {
            try {
                optimChokeUnchoke();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void optimChokeUnchoke() throws InterruptedException, IOException {
        if (nm.optimisticNeighbor != null) {
            nm.optimisticNeighbor.connection.sendMessage(new PeerMessage(PeerMessage.Type.CHOKE, new byte[0]));
        }
        if (nm.getNeighbors().size() <= nm.unchoked.size()) {
            return;
        }
        boolean cont = false;
        for (Neighbor n : nm.getNeighbors()) {
            if (n.theyInterest == PeerMessage.Type.INTERESTED) {
                cont = true;
                break;
            }
        }
        if (!cont) {
            if (us.hasFile && nm.optimisticNeighbor != null)
                System.exit(1);
            return;
        }
        int index = new Random().nextInt(nm.getNeighbors().size());
        while (nm.unchoked.contains(nm.getNeighbors().get(index)) || !nm.getNeighbors().get(index).isInit)
            index = new Random().nextInt(nm.getNeighbors().size());
        nm.optimisticNeighbor = nm.getNeighbors().get(index);
        Logger.logOptChangeNeighbor(us.id, nm.optimisticNeighbor.id);
        nm.optimisticNeighbor.connection.sendMessage(new PeerMessage(PeerMessage.Type.UNCHOKE, new byte[0]));
    }


    public static Long getTime() {
        return System.nanoTime();
    }
}

