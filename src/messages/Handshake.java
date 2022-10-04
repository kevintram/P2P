package messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static messages.Util.byteArrToInt;

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
            baos.write("P2PFILESHARINGPROJ".getBytes());

            // write 10-byte 0 bits
            for (int i = 0; i < 10; i++) {
                baos.write(0);
            }

            // write peer id
            baos.write(ByteBuffer.allocate(4).putInt(id).array());
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
