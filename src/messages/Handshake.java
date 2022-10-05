package messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static messages.Util.byteArrToInt;
import static messages.Util.intToByteArr;

public class Handshake {
    public String header;
    public int id;

    public Handshake(byte[] b) {
        byte[] headerBuf = Arrays.copyOf(b, 18);
        header = new String(headerBuf);

        byte[] idBuf = Arrays.copyOfRange(b, 28, 32);
        id = byteArrToInt(idBuf);
    }

    public Handshake(int id) {
        header = "P2PFILESHARINGPROJ";
        this.id = id;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // write header
            baos.write(header.getBytes());

            // write 10 bytes of 0 bits
            for (int i = 0; i < 10; i++) {
                baos.write(0);
            }

            // write peer id
            baos.write(intToByteArr(id));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return baos.toByteArray();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != Handshake.class) {
            return false;
        } else {
            Handshake other = (Handshake) obj;
            boolean hasRightHeader = other.header.equals(header);
            boolean hasRightId = other.id == id;

            return hasRightHeader && hasRightId;
        }
    }
}
