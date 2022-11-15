package talkers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

public class PieceFileHelper {

    //creates file at given path
    public static boolean createPieceFile(String path, int index) throws IOException {
        //creates a temp file, then sets it to delete on virtual machine termination
        File temp = new File(path + index + ".tmp");
        temp.createNewFile();
        temp.deleteOnExit();
        return true;
    }

    // appends data to the end of the piece
    public static boolean updatePieceFile(String path, int index, byte[] data){
        try{
            FileWriter temp = new FileWriter(path + File.separator + index + ".tmp", true);
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
    public static byte[] getByteArrOfPiece(String path, int index) {
        try {
            return Files.readAllBytes(new File(path + index + ".tmp").toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param finalFile file name of the final file to download
     * @param path path to the temp files and where final file will go
     * @return returns true if it worked
     */
    public static boolean combine(String finalFile, String path) {
        try {
            new FileWriter(path + File.separator + finalFile, false).close();
            FileWriter file = new FileWriter(path + File.separator + finalFile, true);
            for(int i = 0; i < State.numPieces; i++){
                byte[] temp = getByteArrOfPiece(path, i);
                for (byte b : temp) {
                    file.write(b);
                }
            }
            if(State.finalPieceSize > 0){
                byte[] temp = getByteArrOfPiece(path, State.numPieces + 1);
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
}
