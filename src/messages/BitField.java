package messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static messages.Util.intToByteArr;

/*TODO: Tyler
*  Compare bitfields, determine which has what you dont
*  update bitfields based on new data
* */

public class BitField {
    public byte[] bitfield;
    int size;
    /*
    returns an arraylist of block indices for pieces you dont have, arraylist is empty if they have nothing new
     */
    public static ArrayList<Integer> hasNew(byte[] bf1, byte[] bf2, int len){
        ArrayList<Integer> indices = new ArrayList<>();
        for(int i = 0; i < len; i++) {
            if(bf2[i] == 1 && bf1[i] == 0){
                indices.add(i);
            }
        }
        return indices;
    }

}