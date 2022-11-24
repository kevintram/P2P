package peer;

public class Peer {
    public final int id;
    public final String hostName;
    public final int port;
    public boolean hasFile;
    private byte[] bitfield;

    public double downloadRate; // this will be set to -1 UNLESS the file has completed download

    public Long startTime;

    private boolean locked = false;
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


    public byte[] getBitfield() throws InterruptedException {
        //to ensure a read happens after a write, lock until writer done
        if(locked)
            wait();
        return bitfield;
    }

    public void updateBitfield(int index) {
        locked = true;
        this.bitfield[index] = 1;
        locked = false;
        notifyAll(); //tells readers they can read again
    }

    public void setBitfield(byte[] newField) {
        locked = true;
        this.bitfield = newField;
        locked = false;
        notifyAll();
    }

    public void pendingBitfield(int index) {
        locked = true;
        this.bitfield[index] = -1;
        locked = false;
        notifyAll(); //tells readers they can read again
    }
}