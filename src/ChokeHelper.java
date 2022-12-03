import logger.Logger;
import messages.PeerMessage;
import neighbor.NeighborManager;
import peer.Neighbor;
import peer.Peer;

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
        time.schedule(ct, unchokeInterval * 1000);
        time.schedule(ot, optimisticInterval * 1000);
    }

 //sorts by download rate, if 2 peers have same rate, 50/50 chance for order
   static class SortbyDownload implements Comparator<Peer> {
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
            System.out.println("ChokeTask");
            unchokeChoke();
        }
    }

   //idk a good name for this, clears the unchoked array, the recreates it from neighbor list
   public void unchokeChoke(){
       //us.updateDownloadRate(P2P.pfm.numPieces);
       for(Neighbor p : P2P.nm.unchoked){
           Logger.logChoke(p.id, us.id);
           p.connection.sendMessage(new PeerMessage(PeerMessage.Type.CHOKE, new byte[0]));
       }
       nm.unchoked.clear();
       if(!us.hasFile){
           //sorts neighbors by download rate, and picks the top n
           downloadList = nm.getNeighbors();
           Collections.sort(downloadList, new SortbyDownload());
           nm.unchoked = downloadList.subList(0, numPrefNeighbors);
           for(Neighbor nbr : nm.getNeighbors()){
               nbr.downloadRate = 0;
           }
       } else {
           //picks random neighbors to unchoke
           int[] index = new int[numPrefNeighbors];
           System.out.println("index size: " + numPrefNeighbors);
           for(int i = 0; i < numPrefNeighbors; i++){
               index[i] = new Random().nextInt(nm.getNeighbors().size());
           }
           for(int i : index){
               System.out.println("adding: " + i);
               nm.unchoked.add(nm.getNeighbors().get(i));
           }
       }
       for(Neighbor p : nm.unchoked){
           Logger.logUnchoke(p.id, us.id);
           p.connection.sendMessage(new PeerMessage(PeerMessage.Type.UNCHOKE, new byte[0]));
       }
   }
    public class OptimeChokeTask extends TimerTask {

        @Override
        public void run() {
            System.out.println("OptiChokeTask");
            optimChokeUnchoke();
        }
    }

   public void optimChokeUnchoke(){
       if (nm.optimisticNeighbor != null){
           Logger.logChoke(nm.optimisticNeighbor.id, us.id);
           nm.optimisticNeighbor.connection.sendMessage(new PeerMessage(PeerMessage.Type.CHOKE, new byte[0]));
       }
       int index = new Random().nextInt(nm.getNeighbors().size());
       //guarantees the index wont be a part of unchoked because Neighbors array is sorted
       while(nm.unchoked.contains(nm.getNeighbors().get(index)))
           index = new Random().nextInt(nm.getNeighbors().size());
       nm.optimisticNeighbor = nm.getNeighbors().get(index);
       Logger.logUnchoke(nm.optimisticNeighbor.id, us.id);
       nm.optimisticNeighbor.connection.sendMessage(new PeerMessage(PeerMessage.Type.UNCHOKE, new byte[0]));
   }

   public static Long getTime(){
       return System.nanoTime();
   }
}
