package peer;

import java.util.ArrayList;

/*TODO: Tyler
*  Compare bitfields, determine which has what you don't
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
    /*
    returns an arraylist of what we don't have, we send have for everything we have they dont
     */
    public static ArrayList<Integer> doesntHave(byte[] bf1, byte[] bf2, int len){
        ArrayList<Integer> indices = new ArrayList<>();
        for(int i = 0; i < len; i++) {
            if(bf1[i] == 1 && bf2[i] == 0){
                indices.add(i);
            }
        }
        return indices;
    }

}