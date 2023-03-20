package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
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
    public static RawMessage of(long timeStampNs, byte[] bytes) {
        Crc24 crc = new Crc24(Crc24.GENERATOR);
        return crc.crc(bytes) != 0 ? null : new RawMessage(timeStampNs, new ByteString(bytes));
    }

    /**
     * Returns valid length of message if DF is worth 17, otherwise null.
     *
     * @param byte0 the first byte of the message
     * @return the valid length of message if DF is worth 17, otherwise null.
     */
    public static int size(byte byte0) {
            return (byte0 & 0b11111000) == 0b10001000 ? LENGTH : 0;
    }

    /**
     * Returns the time stamp of the message.
     *
     * @return the time stamp of the message
     */
    public int downLinkFormat() {
        return ((bytes.byteAt(0) >>> 3) & 0b11111);
    }

    /**
     * Returns the ICAO address of the message.
     *
     * @return the ICAO address of the message
     */
    public IcaoAddress icaoAddress() {
        String address = Long.toHexString(bytes.bytesInRange(1,4)).toUpperCase();
        address = "000000".substring(address.length()) + address;
        return new IcaoAddress(address);
    }

    /**
     * Returns the payload of the message.
     *
     * @return the payload of the message
     */
    public long payload() {
        return bytes.bytesInRange(4, 11);
    }

    /**
     * Returns the type code of the message.
     *
     * @return the type code of the message
     */
    public int typeCode() {
        return typeCode(payload());
    }

    public static int typeCode(long payload) {
        return Bits.extractUInt (payload, 51, 5);
    }
}