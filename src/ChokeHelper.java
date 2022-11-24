import messages.PeerMessage;
import neighbor.NeighborManager;
import peer.Neighbor;
import peer.Peer;

import java.util.*;

public class ChokeHelper {

    NeighborManager nm;
    Peer us;
    public int numPrefNeighbors;
    public int unchokeInterval;
    public int optimisticInterval;
    public Runnable chokeUnchokeInterval;
    Runnable optimChokeUnchokeInterval;

    public ChokeHelper(NeighborManager nm, Peer us) {
        this.nm = nm;
        this.us = us;
        us.startTime = ChokeHelper.getTime();
        Timer timer = new Timer();
        this.chokeUnchokeInterval = new Runnable() {
            @Override
            public void run() {
                unchokeChoke();
            }
        };
        this.optimChokeUnchokeInterval = new Runnable() {
            @Override
            public void run() {
                optimChokeUnchoke();
            }
        };
    }

 //
   //sorts by download rate, if 2 peers have same rate, 50/50 chance for order
   static class SortbyDownload implements Comparator<Peer> {
       public int compare(Peer a, Peer b){
           if(a.downloadRate == b.downloadRate){
               return new Random().nextInt(100) >= 50 ? -1 : 1;
           }
           return (int)(a.downloadRate - b.downloadRate);
       }
   }
   //idk a good name for this, clears the unchoked array, the recreates it from neighbor list
   public void unchokeChoke(){
       for(Neighbor p : nm.unchoked){
           //TODO send choke message
           //p.connection.sendMessage(new PeerMessage(0, PeerMessage.Type.CHOKE, Optional.empty()));
       }
       nm.unchoked.clear();
       if(!us.hasFile){
           //sorts neighbors by download rate, and picks the top n
           int left = numPrefNeighbors;
           Collections.sort(nm.getNeighbors(), new SortbyDownload());
           nm.unchoked = nm.getNeighbors().subList(0, numPrefNeighbors);
       } else {
           //picks random neighbors to unchoke
           int[] index = new int[numPrefNeighbors];
           for(int i = 0; i < numPrefNeighbors; i++){
               index[i] = new Random().nextInt(nm.getNeighbors().size());
           }
           for(int i : index){
               nm.unchoked.add(nm.getNeighbors().get(i));
           }
       }
       for(Neighbor p : nm.unchoked){
           //TODO send unchoke message
           p.connection.sendMessage(new PeerMessage(PeerMessage.Type.UNCHOKE, Optional.empty()));
       }
   }

   public void optimChokeUnchoke(){
       if (nm.optimisticNeighbor != null){
           nm.optimisticNeighbor.connection.sendMessage(new PeerMessage(PeerMessage.Type.CHOKE, Optional.empty()));
       }
       int index = new Random().nextInt(nm.getNeighbors().size() - numPrefNeighbors);
       index += numPrefNeighbors;
       boolean found = false;
       while(!found){
           if(nm.getNeighbors().get(index).interested){
               nm.optimisticNeighbor = nm.getNeighbors().get(index);
           }
       }
       nm.optimisticNeighbor.connection.sendMessage(new PeerMessage(PeerMessage.Type.UNCHOKE, Optional.empty()));
   }

   public static Long getTime(){
       return System.nanoTime();
   }
}
