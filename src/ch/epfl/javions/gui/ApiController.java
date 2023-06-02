package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.CallSign;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public class ApiController {
    private static final String API_KEY = "8a36a1157fmsh987ccb4343aaa6cp12a5dbjsn3f3125405bee";
    private static final ApiController INSTANCE = new ApiController();

    public final ObservableMap<CallSign, ApiData> memory = FXCollections.observableHashMap();

    private ApiController() {}

    public static ApiController getInstance() {
        return INSTANCE;
    }

    public static ApiData getData(CallSign callSign) {
        if (callSign == null) throw new IllegalArgumentException("CallSign cannot be null");

        try {
            HttpResponse<String> response = sendRequest(callSign);
            return parseJson(response.body());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static HttpResponse<String> sendRequest(CallSign callSign) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(createUri(callSign))
                .header("X-RapidAPI-Key", API_KEY)
                .header("X-RapidAPI-Host", "aerodatabox.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static URI createUri(CallSign callSign) {
        return URI.create(String.format("https://aerodatabox.p.rapidapi.com/flights/callsign/%s?withAircraftImage=true&withLocation=false", callSign.string()));
    }

    private static ApiData parseJson(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);

        String name = rootNode.get(0).get("airline").get("name").asText();
        Airport departure = createAirport(rootNode.get(0).get("departure"));
        Airport arrival = createAirport(rootNode.get(0).get("arrival"));

        return new ApiData(name, departure, arrival);
    }

    private static Airport createAirport(JsonNode airportNode) {
        String iata = airportNode.get("airport").get("iata").asText();
        String municipalityName = airportNode.get("airport").get("municipalityName").asText();
        return new Airport(iata, municipalityName);
    }

    public record ApiData(String name, Airport departure, Airport arrival) {
        @Override
        public String toString() {
            return "Flight Details:\n" +
                    "Name: " + name + "\n" +
                    departure.IATA + " -> " + arrival.IATA;
        }
    }

    public record Airport(String IATA, String municipalityName) {}
}
