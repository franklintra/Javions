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

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public class ObservableAircraftStateTest {

    @Test
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
    void etape7Test() {
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/messages_20230318_0915.bin"))))) {
            byte[] bytes = new byte[RawMessage.LENGTH];

            AircraftStateManager stateManager = new AircraftStateManager(new AircraftDatabase(getClass().getResource("/aircraft.zip").getPath()));

            for (int i = 0; i < 100; i++) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);

                stateManager.updateWithMessage(Objects.requireNonNull(MessageParser.parse(RawMessage.of(timeStampNs, bytes))));

                System.out.println(stateManager.states());
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

//    @Test
//    void updateTable() {
//
//        try (DataInputStream s = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/messages_20230318_0915.bin"))))){
//            AircraftStateManager aircraftStateManager = new AircraftStateManager(new AircraftDatabase(s.toString())); // FIXME: 4/11/2023 this is incorrect, unsure
//            int counter = 0;
//            byte[] bytes = new byte[RawMessage.LENGTH];
//            while (s.available() >= bytes.length) {
//                long timeStampNs = s.readLong();
//                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
//                RawMessage rawMessage = RawMessage.of(timeStampNs, bytes);
//                Message message = MessageParser.parse(rawMessage);
//                aircraftStateManager.updateWithMessage(message);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
    @Test
    public void test1() throws IOException{
        ObservableSet<ObservableAircraftState> states = null;
        int count= 0;
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/messages_20230318_0915.bin"))))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            AircraftDatabase database = new AircraftDatabase(getClass().getResource("/aircraft.zip").getPath());
            AircraftStateManager aircraftStateManager = new AircraftStateManager(database);
            states = aircraftStateManager.states();
            while (s.available() >= bytes.length) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assertEquals(RawMessage.LENGTH, bytesRead);
                ByteString message = new ByteString(bytes);
                Message m = MessageParser.parse(new RawMessage(timeStampNs,message));
                if (m!=null){
                    //count++;
                    aircraftStateManager.updateWithMessage(m);
                    aircraftStateManager.purge();
                    states = aircraftStateManager.states();
                }

            }
        } catch (EOFException e) {
            System.out.println(e.getMessage());
        }
        finally {
            List<ObservableAircraftState> states2 = new ArrayList<>(states);
            states2.sort(new AddressComparator());
            for(ObservableAircraftState o: states2){
                count++;
                AircraftData data = o.getAircraftData();
                if (data != null) {
                    //System.out.println(count);
                    GeoPos p = o.getPosition();
                    String registration = o.getRegistration()!=null ? o.getRegistration().string() : "";

                    System.out.printf("%5s %9s %7s %35s %20s %20s %9.0f %9.0f\n", o.getIcaoAddress().toString(), nullCallSign(o.getCallSign()), registration, data.model(), nullPosLon(p), nullPosLat(o.getPosition()), (o.getAltitude()), (Units.convertTo(o.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)));
                }
                else {
                    GeoPos p = o.getPosition();
                    System.out.printf("%5s %9s %7s %35s %20s %20s %9.0f %9.0f\n", o.getIcaoAddress().toString(), nullCallSign(o.getCallSign()), "", "", nullPosLon(p), nullPosLat(o.getPosition()), (o.getAltitude()), (Units.convertTo(o.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)));

                }
            }
            System.out.println(count);
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
