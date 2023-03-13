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

    static RawMessage of(long timeStampNs, byte[] bytes) {
        Crc24 crc = new Crc24(Crc24.GENERATOR); // TODO: 3/13/2023 check generator
        return crc.crc(bytes) != 0 ? null : new RawMessage(timeStampNs, new ByteString(bytes));
        // return raw ADS B message with given timestamp and bytes
    }

    static int size(byte byte0) {
            int df = byte0 & 0b11111000;
            return df == 0b10001000 ? LENGTH : 0;
    }

    static int typeCode(long payload) { // TODO: 3/13/2023
        // extract the type code from the ME attribute
        long me = (payload >> 32) & 0x00FFFFFF80L;
        return (int) (me >> 8);
        //return (int) (payload >>> 32) & 0x1f;
    }

    public int downlinkFormat() {
        return  bytes.byteAt(0) & 0b11111000;
    }

    public IcaoAddress icaoAddress() {
    return new IcaoAddress(Long.toString(bytes.bytesInRange(1,3)));
    }

    public long payload() {
        return bytes.bytesInRange(4, 10);
    }

    public int typeCode() { // TODO: 3/13/2023
        return typeCode(payload());
    }

}
