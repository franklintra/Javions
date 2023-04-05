package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * @author @chukla (357550)
 * @project Javions
 */
public record RawMessage(long timeStampNs, ByteString bytes) {
    public static final int LENGTH = 14; // length of ADS-B message in bytes
    private static final int DF = 17; // downlink format of known-type ADS-B message
    private static final int PAYLOAD_START_BYTE = 4;
    private static final int PAYLOAD_END_BYTE = 11;

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
    public static RawMessage of(long timeStampNs, byte[] bytes) {
        Crc24 crc = new Crc24(Crc24.GENERATOR);
        return crc.crc(bytes) == 0 ? new RawMessage(timeStampNs, new ByteString(bytes)) : null;
    }

    /**
     * Returns valid length of message if DF is worth 17, otherwise null.
     *
     * @param byte0 the first byte of the message
     * @return the valid length of message if DF is worth 17, otherwise null.
     */
    public static int size(byte byte0) {
        return downLinkFormat(byte0) == DF ? LENGTH : 0;
    }

    /**
     * Returns the time stamp of the message.
     *
     * @return the time stamp of the message
     */
    public int downLinkFormat() {
        return downLinkFormat((byte) bytes.byteAt(0));
    }

    /**
     * Returns the downlink format of the message from the first byte.
     *
     * @param byte0 the first byte of the message
     * @return the downLinkFormat
     */
    private static int downLinkFormat(byte byte0) {
        return ((byte0 >>> 3) & 0b11111);
    }

    /**
     * Returns the ICAO description of the message.
     *
     * @return the ICAO description of the message
     */
    public IcaoAddress icaoAddress() {
        // extract the 24-bit ICAO address from the message (byte 1 to byte 3 inclusive)
        String address = Long.toHexString(bytes.bytesInRange(1, 4)).toUpperCase();
        // pad with zeros to 6 characters if necessary in case the ICAO address encoded is ABC3 for exemple it's meant to be 00ABC3
        address = "000000".substring(address.length()) + address;
        return new IcaoAddress(address);
    }

    /**
     * Returns the payload of the message.
     *
     * @return the payload of the message
     */
    public long payload() {
        // extract the 56-bit payload from the message (byte 4 to byte 10 inclusive)
        return bytes.bytesInRange(PAYLOAD_START_BYTE, PAYLOAD_END_BYTE);
    }

    /**
     * Returns the type code of the message.
     *
     * @return the type code of the message
     */
    public int typeCode() {
        return typeCode(payload());
    }

    /**
     * Extract the 5 leftmost bits of the payload (bits 51 to 55 inclusive) that represent the type code
     *
     * @param payload the ME attribute of the message
     * @return the type code of the message
     */
    public static int typeCode(long payload) {
        return Bits.extractUInt(payload, 51, 5);
    }
}