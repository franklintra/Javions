package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IcaoAddressTest {
    @Test
    void icaoAddressConstructorThrowsWithInvalidAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoAddress("00000a");
        });
    }

    @Test
    void icaoAddressConstructorThrowsWithEmptyAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoAddress("");
        });
    }

    @Test
    void icaoAddressConstructorAcceptsValidAddress() {
        assertDoesNotThrow(() -> {
            new IcaoAddress("ABCDEF");
        });
    }
}