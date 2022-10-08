package messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static messages.Util.intToByteArr;

public class BitField {
    public byte[] bitfield;
    int size;
    public BitField(byte[] bytes, int size) {
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
