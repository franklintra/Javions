package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * @author @franklintra
 * @project Javions
 */
public interface Message {
    // The modifiers public and abstract are not explicitly written in the interface because all interface methods are public and abstract by default.

    /**
     * @return the timeStampNs of the message in nanoseconds
     */
    long timeStampNs();

    /**
     * @return the icaoAddress of the aircraft that sent the message
     */
    IcaoAddress icaoAddress();
}