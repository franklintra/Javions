package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * @author @franklintra
 * @project Javions
 */
public interface Message {

    public abstract long timeStampNs();

    public abstract IcaoAddress icaoAddress();
}
