import messages.PeerMessage;
import neighbor.NeighborManager;
import peer.Neighbor;
import peer.Peer;

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

        while (!nm.haveAllBitfields()) {} // wait until all connections are established

        ChokeUnchokeTask ct = new ChokeUnchokeTask();
        OptimChokeTask ot = new OptimChokeTask();
        time.schedule(ct, 0,unchokeInterval * 1000);
        time.schedule(ot, 0,optimisticInterval * 1000);
    }

 //sorts by download rate, if 2 peers have same rate, 50/50 chance for order
   static class SortByDownload implements Comparator<Peer> {
       public int compare(Peer a, Peer b){
           if(a.downloadRate == b.downloadRate){
               return new Random().nextInt(100) >= 50 ? -1 : 1;
           }
           return (int)(a.downloadRate - b.downloadRate);
       }
   }

    public class ChokeUnchokeTask extends TimerTask {

        @Override
        public void run() {
            unchokeChoke();
        }
    }

   //idk a good name for this, clears the unchoked array, the recreates it from neighbor list
   public void unchokeChoke() {
        List<Neighbor> newUnchoked = new ArrayList<>();

       if (!us.hasFile){
           //sorts neighbors by download rate, and picks the top n
           ArrayList<Neighbor> temp = new ArrayList<>(nm.getNeighbors());
           Collections.sort(temp, new SortByDownload());
           newUnchoked = temp.subList(0, numPrefNeighbors);

           for (Neighbor nbr : nm.getNeighbors()) { // reset download rates
               nbr.downloadRate = 0;
           }
       } else {
           //picks random neighbors to unchoke
           int[] index = new int[numPrefNeighbors];
           for(int i = 0; i < numPrefNeighbors; i++){
               index[i] = new Random().nextInt(nm.getNeighbors().size());
           }

           List<Neighbor> neighbors = nm.getNeighbors();
           for(int i : index){
               newUnchoked.add(neighbors.get(i));
           }
       }

       // send choke message to neighbors that used to be unchoked but are now choked
       for (Neighbor n : nm.unchoked) {
           if (!newUnchoked.contains(n)) {
               n.connection.sendMessage(new PeerMessage(PeerMessage.Type.CHOKE, new byte[0]));
           }
       }

       // send unchoke message to neighbors that used to be choked but are now unchoked
       for (Neighbor n : newUnchoked) {
           if (!nm.unchoked.contains(n)) {
               n.connection.sendMessage(new PeerMessage(PeerMessage.Type.UNCHOKE, new byte[0]));
           }
       }

       nm.unchoked = newUnchoked;
   }

    public class OptimChokeTask extends TimerTask {

        @Override
        public void run() {
            optimChokeUnchoke();
        }
    }

   public void optimChokeUnchoke() {
       if (nm.optimNbr != null) {
           nm.optimNbr.connection.sendMessage(new PeerMessage(PeerMessage.Type.CHOKE, new byte[0]));
       }
       int index = new Random().nextInt(nm.getNeighbors().size());

       //guarantees the index wont be a part of unchoked because Neighbors array is sorted
       while(nm.unchoked.contains(nm.getNeighbors().get(index))) {
           index = new Random().nextInt(nm.getNeighbors().size());
       }

       nm.optimNbr = nm.getNeighbors().get(index);

       if (nm.optimNbr != null) {
           nm.optimNbr.connection.sendMessage(new PeerMessage(PeerMessage.Type.UNCHOKE, new byte[0]));
       }
   }

   public static Long getTime(){
       return System.nanoTime();
   }
}
