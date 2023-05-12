package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.*;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public class ObservableAircraftStateTest {
    public static void main (String[] args) throws IOException {
        new ObservableAircraftStateTest().test();
        new ObservableAircraftStateTest().printTable();
    }

    void test() {
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/messages_20230318_0915.bin"))))) {
            int counter = 0;
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (s.available() >= bytes.length) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assertEquals(RawMessage.LENGTH, bytesRead);
                ByteString message = new ByteString(bytes);
                if (counter++ < 3) System.out.printf("%13d: %s\n", timeStampNs, message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void printTable() {
        long lastMessage = 0;
        AircraftStateManager aircraftStateManager = new AircraftStateManager(new AircraftDatabase(getClass().getResource("/aircraft.zip").getPath()));
        ObjectProperty<ObservableAircraftState> selectedAircraftState = new SimpleObjectProperty<>();
        long lastTime = 0;
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/messages_20230318_0915.bin"))))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (s.available() >= bytes.length) {
                long timeStampNs = s.readLong();
                /*long timeDifference = TimeUnit.NANOSECONDS.toMillis(timeStampNs - lastMessage);
                try {
                    Thread.sleep(timeDifference);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                lastMessage = timeStampNs;
                s.readNBytes(bytes, 0, bytes.length);
                ByteString message = new ByteString(bytes);
                RawMessage rawMessage = new RawMessage(timeStampNs, message);
                Message m = null;
                m = MessageParser.parse(rawMessage);
                if (m == null) continue;
                aircraftStateManager.updateWithMessage(m);
                aircraftStateManager.purge();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    private void printStatesAndClear(PrintStream printer, ObservableSet<ObservableAircraftState> states) {
        clearTerminal(printer);
        Set<ObservableAircraftState> sortedStates = new TreeSet<>(new AddressComparator());
        sortedStates.addAll(states);
        //clearTerminal(System.out);
        printer.println("ICAO    CALLSIGN REGISTRATION                        MODEL            LONGITUDE                LATITUDE     ALTITUDE  SPEED");
        sortedStates.forEach(o -> {
            AircraftData data = o.getAircraftData();
            GeoPos p = o.getPosition();
            if (data != null) {
                String registration = o.getRegistration()!=null ? o.getRegistration().string() : "";
                printer.printf("%5s %9s %7s %35s %20s %20s %9.0f %9.0f\n", o.getIcaoAddress().string(), nullCallSign(o.getCallSign()), registration, data.model(), nullPosLon(p), nullPosLat(o.getPosition()), (o.getAltitude()), (Units.convertTo(o.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)));
            }
            else {
                printer.printf("%5s %9s %7s %35s %20s %20s %9.0f %9.0f\n", o.getIcaoAddress().string(), nullCallSign(o.getCallSign()), "", "", nullPosLon(p), nullPosLat(o.getPosition()), (o.getAltitude()), (Units.convertTo(o.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)));
            }
        });
    }

    private void clearTerminal(PrintStream printer) {
        String CSI = "\u001B[";
        String CLEAR_SCREEN = CSI + "2J";
        printer.println(CLEAR_SCREEN);
    }

    private static String nullParser(Object s) {
        return s == null ? "" : s.toString();
    }

    private static String nullCallSign(CallSign s) {
        return s == null ? "" : s.string();
    }

    private static String nullVelocity(Long p) {
        return p == null ? "" : String.valueOf(Units.convertTo(p, Units.Speed.KILOMETER_PER_HOUR));
    }

    private static String nullPosLon(GeoPos p) {
        return p == null ? "" : String.valueOf(Units.convertTo(p.longitude(), Units.Angle.DEGREE));
    }

    private static String nullPosLat(GeoPos p) {
        return p == null ? "" : String.valueOf(Units.convertTo(p.latitude(), Units.Angle.DEGREE));
    }

    private static class AddressComparator implements Comparator<ObservableAircraftState> {
        @Override
        public int compare(ObservableAircraftState o1, ObservableAircraftState o2) {
            String s1 = o1.getIcaoAddress().toString();
            String s2 = o2.getIcaoAddress().toString();
            return s1.compareTo(s2);
        }
    }
}
