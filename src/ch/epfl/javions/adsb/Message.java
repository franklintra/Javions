package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * @author @franklintra (362694)
 * @project Javions
 */

/**
 * A Message is an object that represents a message sent by an aircraft.
 * This interface is implemented by all classes that represent a message sent by an aircraft.
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