package peer;

import logger.Logger;
import piece.PieceFileManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Peer {
    public final int id;
    public final String hostName;
    public final int port;
    public boolean hasFile;
    private byte[] bitfield;

    public double downloadRate; // this will be set to -1 UNLESS a piece has been downloaded

    public Long startTime;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private CountDownLatch latch;

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

    public void updateDownloadRate(int numPieces){
        int counterDown = 0;
        int buffer = (8 - (numPieces % 8));
        for(int i = 0; i < bitfield.length-buffer; i++){
            if(bitfield[i] == 1){
                counterDown++;
            }
        }
        this.downloadRate = counterDown / (System.nanoTime() - startTime);
    }


    public byte[] getBitfield() throws InterruptedException {
        lock.readLock().lock();
        //to ensure a read happens after a write, lock until writer done
        if(lock.isWriteLocked())
            latch.await();
        byte [] temp = bitfield;
        lock.readLock().unlock();
        return temp;
    }

    public void updateBitfield(int index) throws InterruptedException {
        if(lock.isWriteLocked())
            latch.await();
        lock.writeLock().lock();
        latch = new CountDownLatch(1);
        this.bitfield[index] = 1;
        lock.writeLock().unlock();
        latch.countDown();
    }

    public void setBitfield(byte[] newField) throws InterruptedException {
        if(lock.isWriteLocked())
            latch.await();
        lock.writeLock().lock();
        latch = new CountDownLatch(1);
        this.bitfield = newField;
        lock.writeLock().unlock();
        latch.countDown();
    }

    public void pendingBitfield(int index) throws InterruptedException {
        if(lock.isWriteLocked())
            latch.await();
        lock.writeLock().lock();
        latch = new CountDownLatch(1);
        this.bitfield[index] = -1;
        lock.writeLock().unlock();
        latch.countDown();
    }


    public boolean finishedFile(int numPieces) {
        int buffer = (8 - (numPieces % 8));
        for(int i = 0; i < bitfield.length-buffer; i++){
            if(bitfield[i] == 0){
                return false;
            }
        }
        Logger.logComplete(this.id);
        return true;
    }
}