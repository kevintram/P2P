package peer;

import talkers.State;

import java.util.*;

public class Peer {
    public final int id;
    public final String hostName;
    public final int port;
    public boolean hasFile;
    public byte[] bitField;

    public double downloadRate; // this will be set to -1 UNLESS the file has completed download

    public Long startTime;



    public Peer(int id, String hostName, int port, boolean hasFile) {
        this.id = id;
        this.hostName = hostName;
        this.port = port;
        this.hasFile = hasFile;
        this.downloadRate = -1.0;
    }

    public void setDownloadRate(float rate){
        this.downloadRate = rate;
    }

}