package messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static messages.Util.intToByteArr;

/*TODO: Tyler
*  Compare bitfields, determine which has what you dont
*  update bitfields based on new data
* */

public class BitField {
    public byte[] bitfield;
    int size;
    public BitField(byte[] bytes, int size) {
        //parses the bytestream sent in, and creates a bitfield in format of 8 bits, space, 8 more bits
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for(int i = 0; i < size; i++){
            baos.write(bytes[i]);
            if(i != 0 && i % 7 == 0){
                baos.write(' ');
            }
        }
        this.size = size;
        this.bitfield = baos.toByteArray();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != BitField.class) {
            return false;
        } else {
            BitField other = (BitField) obj;
            if(other.size != this.size) return false;
            for(int i = 0; i < this.size; i++){
                if(this.bitfield[i] != other.bitfield[i]){
                    return false;
                }
            }
        }
        return true;
    }


}
