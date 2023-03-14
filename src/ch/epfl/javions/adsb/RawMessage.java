package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * @project Javions
 * @author @chukla
 */
public record RawMessage(long timeStampNs, ByteString bytes) {
    public static final int LENGTH = 14; // length of ADS-B message in bytes

    public RawMessage {
        Preconditions.checkArgument(timeStampNs >= 0 && bytes.size() == LENGTH);
    }

    /**
     * Returns a RawMessage object if the CRC24 of the bytes is 0, null otherwise.
     *
     * @param timeStampNs the time stamp in nanoseconds
     * @param bytes       the 14 bytes of the message
     * @return a RawMessage object if the CRC24 of the bytes is 0, null otherwise
     */
    public
    static RawMessage of(long timeStampNs, byte[] bytes) {
        Crc24 crc = new Crc24(Crc24.GENERATOR); // FIXME: 3/14/2023 check if this is correct and generator
        return crc.crc(bytes) != 0 ? null : new RawMessage(timeStampNs, new ByteString(bytes));

    }

    /**
     * Returns valid length of message if DF is worth 17, otherwise null.
     *
     * @param byte0 the first byte of the message
     * @return the valid length of message if DF is worth 17, otherwise null.
     */
    static int size(byte byte0) {
            int df = byte0 & 0b11111000;
            return df == 0b10001000 ? LENGTH : 0;
    }

    /**
     * Returns the type code of the message.
     *
     * @param payload the payload of the message
     * @return the type code of the message
     */
    static int typeCode(long payload) { // FIXME: 3/14/2023 check if this is correct
        // Bit mask to extract the 5 MSB of a 56-bit long integer
        long msbMask = 0b11111_000000000000000000000000000000000000000000000000000L;
        // Apply the bit mask to the payload and shift the result to the right
        // by 51 bits to obtain the 5 MSB as an integer value
        return (int) ((payload & msbMask) >>> 51);
    }

    /**
     * Returns the downlink format of the message.
     *
     * @return the downlink format of the message
     */
    public int downLinkFormat() {
        return  bytes.byteAt(0) & 0b11111000;
    }

    /**
     * Returns the ICAO address of the message.
     *
     * @return the ICAO address of the message
     */
    public IcaoAddress IcaoAddress() {
    return new IcaoAddress(Long.toString(bytes.bytesInRange(1,3)));
    }

    /**
     * Returns the payload of the message.
     *
     * @return the payload of the message
     */
    public long payload() {
        return bytes.bytesInRange(4, 10);
    }

    /**
     * Returns the type code of the message.
     *
     * @return the type code of the message
     */
    public int typeCode() {
        return typeCode(payload());
    }

}
