package ch.epfl.javions;

import org.junit.jupiter.api.Test;

import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unused")
class Crc24Test {

    private static final HexFormat HEX_FORMAT = HexFormat.of();
    private static final List<String> ADSB_MESSAGES = List.of(
            "8D392AE499107FB5C00439035DB8",
            "8D39DD4158B511FDC118E1A835FE",
            "8D346083F8230006004BB862B42C",
            "8D506CA358B982DBAD9595A23761",
            "8D3CDD2158AF85CA4125E4620E46",
            "8D39CE6B990D91126808450C6A94",
            "8D49411499113AA890044A80894B",
            "8D4CA4EEEA466867791C0845193E",
            "8D484C5058353646A147292758A9",
            "8D47BA78EA4C4864013C084ABCAA",
            "8D0A009C9908673B1808408A5B0D");

    @Test
    void crc24CrcWorksOnAdsbMessages() {
        var crc24 = new Crc24(Crc24.GENERATOR);

        // Pass full messages with valid CRCs, then check that result is 0.
        for (var m : ADSB_MESSAGES) {
            var bs = HEX_FORMAT.parseHex(m);
            assertEquals(0, crc24.crc(bs));
        }

        // Pass messages without CRC, then check for equality.
        for (var m : ADSB_MESSAGES) {
            var actualCrc = crc24.crc(HEX_FORMAT.parseHex(m.substring(0, m.length() - 6)));
            var expectedCrc = HexFormat.fromHexDigits(m.substring(m.length() - 6));
            assertEquals(expectedCrc, actualCrc);
        }
    }

    @Test
    void crc24CrcWorksWithDifferentGenerator() {
        var crc24_FACE51 = new Crc24(0xFACE51);
        var actual_FACE51 = crc24_FACE51.crc(HEX_FORMAT.parseHex(ADSB_MESSAGES.get(0)));
        var expected_FACE51 = 3677528;
        assertEquals(expected_FACE51, actual_FACE51);

        var crc24_F00DAB = new Crc24(0xF00DAB);
        var actual_F00DAB = crc24_F00DAB.crc(HEX_FORMAT.parseHex(ADSB_MESSAGES.get(0)));
        var expected_F00DAB = 16093840;
        assertEquals(expected_F00DAB, actual_F00DAB);
    }

    @Test
    void crc24CrcWorksWithZeroOnlyMessage() {
        var crc24 = new Crc24(Crc24.GENERATOR);
        for (int i = 0; i < 10; i += 1) {
            var m = new byte[i];
            assertEquals(0, crc24.crc(m));
        }
    }
}