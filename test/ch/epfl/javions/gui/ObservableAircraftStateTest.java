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
import javafx.collections.ObservableSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    void etape7Test() {
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/messages_20230318_0915.bin"))))) {
            byte[] bytes = new byte[RawMessage.LENGTH];

            AircraftStateManager stateManager = new AircraftStateManager(new AircraftDatabase(getClass().getResource("/aircraft.zip").getPath()));

            for (int i = 0; i < 100; i++) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);

                stateManager.updateWithMessage(Objects.requireNonNull(MessageParser.parse(Objects.requireNonNull(RawMessage.of(timeStampNs, bytes)))));

                System.out.println(stateManager.states());
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void printTable() throws IOException{
        ObservableSet<ObservableAircraftState> states = null;
        int counter = 0;
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/messages_20230318_0915.bin"))))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            AircraftDatabase database = new AircraftDatabase(Objects.requireNonNull(getClass().getResource("/aircraft.zip")).getPath());
            AircraftStateManager aircraftStateManager = new AircraftStateManager(database);
            while (s.available() >= bytes.length) {
                counter++;
                long timeStampNs = s.readLong();
                s.readNBytes(bytes, 0, bytes.length);
                ByteString message = new ByteString(bytes);
                Message m = MessageParser.parse(new RawMessage(timeStampNs,message));
                if (m == null) continue;
                aircraftStateManager.updateWithMessage(m);
                aircraftStateManager.purge();
                states = aircraftStateManager.states();
                if (100 < counter++ && counter < 600) printStatesAndClear(System.out, states);
            }
        } catch (EOFException e) {
            System.out.println(e.getMessage());
        }
        finally {
            assertNotNull(states);
            printStatesAndClear(System.out, states);
        }
    }

    private void printStatesAndClear(PrintStream printer, ObservableSet<ObservableAircraftState> states) {
        Set<ObservableAircraftState> sortedStates = new TreeSet<>(Comparator.comparing(o -> o.getIcaoAddress().string()));
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
        for (int i = 0; i < 20; i++) {
            printer.println();
        }
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
