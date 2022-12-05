package logger;

import peer.Neighbor;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class Logger {
    public static String getTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
    }

    /**
     * Log function for establishing TCP connection to peer
     * @param hostID ID of the local peer
     * @param connectionID ID of the peer being connected to
     */
    public static void logMakeConnection(int hostID, int connectionID) throws IOException {
        FileWriter writer = new FileWriter("log_peer_"+hostID+".log", true);
        writer.write(String.format("[%s]: Peer [%d] makes a connection to Peer [%d]%n", getTime(), hostID, connectionID));
        writer.close();
    }

    /**
     * Log function for when a peer connects to local
     * @param hostID ID of the local peer
     * @param connectionID ID of the peer being connected to
     */
    public static void logConnectionEstablished(int hostID, int connectionID) throws IOException {
        FileWriter writer = new FileWriter("log_peer_"+hostID+".log", true);
        writer.write(String.format("[%s]: Peer [%d] is connected from Peer [%d]%n", getTime(), hostID, connectionID));
        writer.close();
    }

    /**
     * @param hostID ID of local peer
     * @param peers Array of Peers.
     */
    public static void logChangeNeighbors(int hostID, List<Neighbor> peers) throws IOException {
        FileWriter writer = new FileWriter("log_peer_"+hostID+".log", true);
        writer.write(String.format("[%s]: Peer [%d] has the preferred neighbors [", getTime(), hostID));
        for(int i = 0; i < peers.size(); i++){
            if(i < peers.size() - 1)
                writer.write(String.format("%d,",peers.get(i).id));
            else
                writer.write(String.format("%d",peers.get(i).id));
        }
        writer.write(String.format("]%n"));
        writer.close();
    }

    /**
     * Logs change of optimistic preferred neighbor
     * @param hostID id of local peer
     * @param peerID id of new Optimistic neighbor
     */
    public static void logOptChangeNeighbor(int hostID, int peerID) throws IOException {
        FileWriter writer = new FileWriter("log_peer_"+hostID+".log", true);
        writer.write(String.format("[%s]: Peer [%d] has the optimistically unchoked neighbor [%d]%n", getTime(), hostID, peerID));
        writer.close();
    }

    /**
     * logs when local peer is unchoked
     * @param p1ID Peer.Peer being unchoked
     * @param p2ID Peer.Peer unchoking
     */
    public static void logUnchoke(int p1ID, int p2ID) throws IOException {
        FileWriter writer = new FileWriter("log_peer_"+p1ID+".log", true);
        writer.write(String.format("[%s]: Peer [%d] is unchoked by [%d]%n", getTime(), p1ID, p2ID));
        writer.close();
    }

    /**
     * log for when a peer chokes another peer
     * @param p1ID peer being choked
     * @param p2ID peer doing the choking
     */
    public static void logChoke(int p1ID, int p2ID) throws IOException {
        FileWriter writer = new FileWriter("log_peer_"+p1ID+".log", true);
        writer.write(String.format("[%s]: Peer [%d] is choked by [%d]%n", getTime(), p1ID, p2ID));
        writer.close();
    }

    /**
     * Logs when an interest message is received
     * @param hostID local peer, the one who was given interest
     * @param peerID peer that is interested
     */
    public static void logInterest(int hostID, int peerID) throws IOException {
        FileWriter writer = new FileWriter("log_peer_"+hostID+".log", true);
        writer.write(String.format("[%s]: Peer [%d] received the 'interested' message from [%d]%n", getTime(), hostID, peerID));
        writer.close();
    }


    /**
     * Logs when a not interest message is received
     * @param hostID local peer, the one who was given not interest message
     * @param peerID peer that is not interested
     */
    public static void logNotInterest(int hostID, int peerID) throws IOException {
        FileWriter writer = new FileWriter("log_peer_"+hostID+".log", true);
        writer.write(String.format("[%s]: Peer [%d] received the 'not interested' message from [%d]%n", getTime(), hostID, peerID));
        writer.close();
    }

    /**
     * logs when a have message is received
     * @param hostID peer receiving have
     * @param peerID peer sending have
     */
    public static void logHave(int hostID, int peerID) throws IOException {
        FileWriter writer = new FileWriter("log_peer_"+hostID+".log", true);
        writer.write(String.format("[%s]: Peer [%d] received the 'have' message from [%d]%n", getTime(), hostID, peerID));
        writer.close();
    }


    /**
     * Logs whenever local host downloads piece from neighbor
     * @param hostID id of local peer
     * @param peerID id of neighbor peer
     * @param pieceIndex index of piece being downloaded
     */
    public static void logDownload(int hostID, int peerID, int pieceIndex) throws IOException {
        FileWriter writer = new FileWriter("log_peer_"+hostID+".log", true);
        writer.write(String.format("[%s]: Peer [%d] has downloaded the piece [%d] from [%d]%n", getTime(), hostID, pieceIndex, peerID));
        writer.close();
    }

    /**
     * Logs when file download is complete
     * @param hostID local peer
     */
    public static void logComplete(int hostID) throws IOException {
        FileWriter writer = new FileWriter("log_peer_"+hostID+".log", true);
        writer.write(String.format("[%s]: Peer [%d] has downloaded the complete file%n", getTime(), hostID));
        writer.close();
    }
}
