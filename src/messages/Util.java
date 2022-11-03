package messages;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Util {
    public static int byteArrToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF) << 0 );
    }

    public static byte[] intToByteArr(int i ) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }
}
