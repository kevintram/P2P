import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileHandler {

    //creates file at given path
    public static boolean createPieceFile(String path, int index, Optional<byte[]> data){
        try{
            //creates a temp file, then sets it to delete on virtual machine termination
            File temp = File.createTempFile(Integer.toString(index),"tmp" ,new File(path));
            temp.deleteOnExit();
            if(data.isPresent()){
                FileWriter wr = new FileWriter(temp);
                for(byte b : data.get()){
                    wr.write(b);
                }
                wr.close();
            }
        }
        catch(IOException e){
            System.out.println("failed to create piece");
            return false;
        }
        return true;
    }
    //write the whole piece
    //TODO look into modifying a half-written piece file
    public static boolean updatePieceFile(String path, int index, byte[] data){
        try{
            FileWriter temp = new FileWriter(String.format("%s\\%i.tmp", path, index));
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

    /**
     * @param pieceCnt number of pieces
     * @param finalFile file name of the final file to downlaod
     * @param path path to the temp files and where final file will go
     * @return returns true if it worked
     */
    public static boolean combine(int pieceCnt, String finalFile, String path){
        try{
            FileWriter file = new FileWriter(finalFile);
            for(int i = 0; i < pieceCnt; i++){
                byte[] temp = Files.readAllBytes(Paths.get(String.format("%s\\%i.tmp", path, i)));
            }
        } catch (IOException e){
            System.out.println("Failed to combine file");
            return false;
        }
        return true;
    }
}
