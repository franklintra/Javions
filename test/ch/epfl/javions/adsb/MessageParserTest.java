package ch.epfl.javions.adsb;

import ch.epfl.javions.Crc24;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageParserTest {
    // Code to generate the variants of the messages used below.
    List<String> rawMessageWithTypeCodes(String baseMessage, int... typeCodes) {
        var crcComputer = new Crc24(Crc24.GENERATOR);
        var message = HexFormat.of().parseHex(baseMessage);
        var variants = new ArrayList<String>();
        for (int typeCode : typeCodes) {
            var byte4 = message[4];
            byte4 = (byte) (typeCode << 3 | (byte4 & 0b111));
            message[4] = byte4;
            var crc = crcComputer.crc(Arrays.copyOfRange(message, 0, 11));
            message[11] = (byte) (crc >> 16);
            message[12] = (byte) (crc >> 8);
            message[13] = (byte) crc;
            var rawMessage = RawMessage.of(100, message);
            variants.add(rawMessage.bytes().toString());
        }
        return variants;
    }

    @Test
    void messageParserParsesAllAircraftIdentificationMessages() {
        var variants = List.of(
                "8D3991E10B0464B1CD43206F07E8",
                "8D3991E1130464B1CD4320B4E75E",
                "8D3991E11B0464B1CD43205714CB",
                "8D3991E1230464B1CD4320FCD23B");
        for (String variant : variants) {
            var rawMessage = RawMessage.of(100, HexFormat.of().parseHex(variant));
            assertNotNull(rawMessage);
            var message = MessageParser.parse(rawMessage);
            assertNotNull(message);
            assertTrue(message instanceof AircraftIdentificationMessage);
        }
    }

    @Test
    void messageParserParsesAllAirbornePositionMessages() {
        var variants = List.of(
                "8D406666480D1652395CBE325E1D",
                "8D406666500D1652395CBEE9BEAB",
                "8D406666580D1652395CBE0A4D3E",
                "8D406666600D1652395CBEA18BCE",
                "8D406666680D1652395CBE42785B",
                "8D406666700D1652395CBE9998ED",
                "8D406666780D1652395CBE7A6B78",
                "8D406666800D1652395CBE0E8C15",
                "8D406666880D1652395CBEED7F80",
                "8D406666900D1652395CBE369F36",
                "8D406666A00D1652395CBE7EAA53",
                "8D406666A80D1652395CBE9D59C6",
                "8D406666B00D1652395CBE46B970");
        for (String variant : variants) {
            var rawMessage = RawMessage.of(100, HexFormat.of().parseHex(variant));
            assertNotNull(rawMessage);
            var message = MessageParser.parse(rawMessage);
            assertNotNull(message);
            assertTrue(message instanceof AirbornePositionMessage);
        }
    }

    @Test
    void messageParserParsesAllAirborneVelocityMessages() {
        var variants = List.of("8D485020994409940838175B284F");
        for (String variant : variants) {
            var rawMessage = RawMessage.of(100, HexFormat.of().parseHex(variant));
            assertNotNull(rawMessage);
            var message = MessageParser.parse(rawMessage);
            assertNotNull(message);
            assertTrue(message instanceof AirborneVelocityMessage);
        }
    }

    @Test
    void messagesParserReturnsNullForUnknownTypeCodes() {
        var variants = List.of(
                "8D48502001440994083817BFA5E8",
                "8D485020294409940838172C703B",
                "8D48502031440994083817F7908D",
                "8D48502039440994083817146318",
                "8D485020414409940838175FE964",
                "8D485020B94409940838172B0E09",
                "8D485020C1440994083817608475",
                "8D485020C94409940838178377E0",
                "8D485020D1440994083817589756",
                "8D485020D9440994083817BB64C3",
                "8D485020E144099408381710A233",
                "8D485020E9440994083817F351A6",
                "8D485020F144099408381728B110",
                "8D485020F9440994083817CB4285");
        for (String variant : variants) {
            var rawMessage = RawMessage.of(100, HexFormat.of().parseHex(variant));
            assertNotNull(rawMessage);
            var message = MessageParser.parse(rawMessage);
            assertNull(message);
        }
    }
}
