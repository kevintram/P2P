package piece;

import peer.Peer;

import java.io.*;
import java.nio.file.Files;

public class PieceFileManager {
    public String fileName;
    public int fileSize;

    public int normalPieceSize;
    public int finalPieceSize; //the leftover piece if file isn't evenly divisible
    public int numPieces;

    public String ourPath;

    public PieceFileManager(String ourPath, String fileName, int pieceSize, int finalPieceSize, int numPieces) {
        this.ourPath = ourPath;
        this.fileName = fileName;
        this.normalPieceSize = pieceSize;
        this.finalPieceSize = finalPieceSize;
        this.numPieces = numPieces;
    }

    public void makePieces(Peer us) throws IOException {

        for(int i = 0; i < numPieces; i++) {
            createPieceFile(ourPath, i);
        }

        // if we have the file, write the file into the pieces
        if (us.hasFile) {
            File theFile = new File(ourPath + File.separator + fileName);
            FileInputStream br = new FileInputStream(theFile);

            for(int i = 0; i < numPieces; i++) {
                int pieceSize = getPieceSizeOf(i);
                byte[] buff = new byte[pieceSize];
                br.read(buff, 0, pieceSize);
                updatePieceFile(i, buff);
            }
            br.close();
        }

        // adds a shutdown hook, so on client termination, temp files will combine if the file is complete
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            boolean complete = true;
            for (int i = 0; i < numPieces; i++){
                try {
                    if(us.getBitfield()[0] == 0){
                        complete = false;
                        break;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (complete) {
                combine(fileName);
                System.out.println("File combined...");
            }
        }));
    }

    //creates file at given path
    public boolean createPieceFile(String path, int index) throws IOException {
        //creates a temp file, then sets it to delete on virtual machine termination
        File temp = new File(path + File.separator + index + ".tmp");
        temp.createNewFile();
        temp.deleteOnExit();
        return true;
    }

    // appends data to the end of the piece
    public boolean updatePieceFile(int index, byte[] data){
        try{
            FileOutputStream temp = new FileOutputStream(ourPath + File.separator + index + ".tmp", true);
            for(byte b : data){
                temp.write(b);
            }
            temp.close();
        } catch (IOException e){
            System.out.println("Failed to update piece");
            return false;
        }

        return true;

    }

    // returns the entire piece as a byte array
    public byte[] getByteArrOfPiece(int index) {
        try {
            return Files.readAllBytes(new File(ourPath + File.separator + index + ".tmp").toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param finalFile file name of the final file to download
     * @return returns true if it worked
     */
    public boolean combine(String finalFile) {
        try {
            new FileWriter(ourPath + File.separator + finalFile, false).close();
            FileOutputStream file = new FileOutputStream(ourPath + File.separator + finalFile, true);
            for(int i = 0; i < numPieces; i++){
                byte[] temp = getByteArrOfPiece(i);
                for (byte b : temp) {
                    file.write(b);
                }
            }
            file.close();

        } catch (IOException e){
            System.out.println("Failed to combine file");
            return false;
        }
        return true;
    }

    private int getPieceSizeOf(int i) {
        if (i == numPieces - 1) {
            return finalPieceSize;
        } else {
            return normalPieceSize;
        }
    }
}
