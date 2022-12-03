package logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Logger {
    public static String getTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
    }

    /**
     * Log function for establishing TCP connection to peer
     * @param hostID ID of the local peer
     * @param connectionID ID of the peer being connected to
     */
    public static void logMakeConnection(int hostID, int connectionID){
        System.out.format("[%s]: Peer [%d] makes a connection to Peer [%d]%n", getTime(), hostID, connectionID);
    }

    /**
     * Log function for when a peer connects to local
     * @param hostID ID of the local peer
     * @param connectionID ID of the peer being connected to
     */
    public static void logConnectionEstablished(int hostID, int connectionID){
        System.out.format("[%s]: Peer [%d] is connected from Peer [%d]%n", getTime(), hostID, connectionID);
    }

    /**
     * @param hostID ID of local peer
     * @param peerIDs Array of Peer.Peer ID's for the new local  neighbors
     */
    public static void logChangeNeighbors(int hostID, int[] peerIDs){
        System.out.format("[%s]: Peer [%d] has the preferred neighbors [", getTime(), hostID);
        for(int id : peerIDs){
            System.out.format("%d",id);
        }
        System.out.format("]%n");
    }

    /**
     * Logs change of optimistic preferred neighbor
     * @param hostID id of local peer
     * @param peerID id of new Optimistic neighbor
     */
    public static void logOptChangeNeighbor(int hostID, int peerID){
        System.out.format("[%s]: Peer [%d] has the optimistically unchoked neighbor [%d]%n", getTime(), hostID, peerID);
    }

    /**
     * logs when local peer is unchoked
     * @param p1ID Peer.Peer being unchoked
     * @param p2ID Peer.Peer unchoking
     */
    public static void logUnchoke(int p1ID, int p2ID){
        //System.out.format("[%s]: Peer [%d] is unchoked by [%d]%n", getTime(), p1ID, p2ID);
    }

    /**
     * log for when a peer chokes another peer
     * @param p1ID peer being choked
     * @param p2ID peer doing the choking
     */
    public static void logChoke(int p1ID, int p2ID){
        //System.out.format("[%s]: Peer [%d] is choked by [%d]%n", getTime(), p1ID, p2ID);
    }

    /**
     * Logs when an interest message is received
     * @param hostID local peer, the one who was given interest
     * @param peerID peer that is interested
     */
    public static void logInterest(int hostID, int peerID){
        //System.out.format("[%s]: Peer [%d] received the 'interested' message from [%d]%n", getTime(), hostID, peerID);
    }


    /**
     * Logs when a not interest message is received
     * @param hostID local peer, the one who was given not interest message
     * @param peerID peer that is not interested
     */
    public static void logNotInterest(int hostID, int peerID){
       //System.out.format("[%s]: Peer [%d] received the 'not interested' message from [%d]%n", getTime(), hostID, peerID);
    }

    /**
     * logs when a have message is received
     * @param hostID peer receiving have
     * @param peerID peer sending have
     */
    public static void logHave(int hostID, int peerID) {
        //System.out.format("[%s]: Peer [%d] received the 'have' message from [%d]%n", getTime(), hostID, peerID);
    }


    /**
     * Logs whenever local host downloads piece from neighbor
     * @param hostID id of local peer
     * @param peerID id of neighbor peer
     * @param pieceIndex index of piece being downloaded
     */
    public static void logDownload(int hostID, int peerID, int pieceIndex){
        //System.out.format("[%s]: Peer [%d] has downloaded the piece [%d] from [%d]%n", getTime(), hostID, pieceIndex, peerID);
    }

    /**
     * Logs when file download is complete
     * @param hostID local peer
     */
    public static void logComplete(int hostID){
        //System.out.format("[%s]: Peer [%d] has downloaded the complete file%n", getTime(), hostID);
    }
}
