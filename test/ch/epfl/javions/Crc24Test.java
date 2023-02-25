package ch.epfl.javions;

import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author @franklintra
 * @project Javions
 */

class Crc24Test {
    /**
     * Test the CRC with all given examples from the instruction set
     */
    @Test
    void crc() {
        String[] messages = new String[]{"8D392AE499107FB5C00439", "8D3950C69914B232880436", "8D4B17E399893E15C09C21", "8D4B18F4231445F2DB63A0", "8D495293F82300020049B8"};
        String[] expectedCrcs = new String[]{"035DB8", "EE2EC6", "BC63D3", "9FC014", "DEEB82", "111203"};
        int c;
        byte[] mAndC;
        byte[] mONly;
        System.out.println("Testing CRC24");
        for (int i = 0; i < messages.length; i++){
            c = Integer.parseInt(expectedCrcs[i], 16);
            mAndC = HexFormat.of().parseHex(messages[i] + expectedCrcs[i]);
            assertEquals(0, Crc24.crc(mAndC));

            mONly = HexFormat.of().parseHex(messages[i]);
            assertEquals(c, Crc24.crc(mONly));
            System.out.println(i+1+"/"+messages.length+"Testing message: " + messages[i] + " with expected CRC: " + expectedCrcs[i]);
        }
    }
}
