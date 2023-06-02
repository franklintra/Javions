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
 * The ApiController class is responsible for managing the API calls.
 * This API allows us to display other data points pertaining to the aircraft.
 */
public class ApiController {
    private static final String API_KEY = "8a36a1157fmsh987ccb4343aaa6cp12a5dbjsn3f3125405bee";
    private static final ApiController INSTANCE = new ApiController();

    /**
     * ObservableMap to store the retrieved API data.
     */
    public final ObservableMap<CallSign, ApiData> memory = FXCollections.observableHashMap();

    private ApiController() {}

    /**
     * Returns the instance of the ApiController class (singleton pattern).
     *
     * @return The instance of the ApiController class.
     */
    public static ApiController getInstance() {
        return INSTANCE;
    }

    /**
     * Retrieves API data for the given CallSign.
     *
     * @param callSign The CallSign of the aircraft.
     * @return The retrieved API data for the given CallSign.
     * @throws IllegalArgumentException if the CallSign is null.
     * @throws RuntimeException if an error occurs during the API call.
     */
    public static ApiData getData(CallSign callSign) {
        if (callSign == null) throw new IllegalArgumentException("CallSign cannot be null");

        try {
            HttpResponse<String> response = sendRequest(callSign);
            return parseJson(response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends an HTTP request to the API to retrieve data for the given CallSign.
     *
     * @param callSign The CallSign of the aircraft.
     * @return The HTTP response containing the API data.
     * @throws Exception if an error occurs during the HTTP request.
     */
    private static HttpResponse<String> sendRequest(CallSign callSign) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(createUri(callSign))
                .header("X-RapidAPI-Key", API_KEY)
                .header("X-RapidAPI-Host", "aerodatabox.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Creates the URI for the API request based on the CallSign.
     *
     * @param callSign The CallSign of the aircraft.
     * @return The URI for the API request.
     */
    private static URI createUri(CallSign callSign) {
        return URI.create(String.format("https://aerodatabox.p.rapidapi.com/flights/callsign/%s?withAircraftImage=true&withLocation=false", callSign.string()));
    }

    /**
     * Parses the JSON response from the API and extracts the relevant data.
     *
     * @param json The JSON response from the API.
     * @return The parsed API data.
     * @throws JsonProcessingException if an error occurs during JSON processing.
     */
    private static ApiData parseJson(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);

        String name = rootNode.get(0).get("airline").get("name").asText();
        Airport departure = createAirport(rootNode.get(0).get("departure"));
        Airport arrival = createAirport(rootNode.get(0).get("arrival"));

        return new ApiData(name, departure, arrival);
    }

    /**
     * Creates an Airport object from the JSON node representing the airport information.
     *
     * @param airportNode The JSON node representing the airport information.
     * @return The created Airport object.
     */
    private static Airport createAirport(JsonNode airportNode) {
        String iata = airportNode.get("airport").get("iata").asText();
        String municipalityName = airportNode.get("airport").get("municipalityName").asText();
        return new Airport(iata, municipalityName);
    }

    /**
     * Represents the data retrieved from the API for a specific CallSign.
     */
    public record ApiData(String name, Airport departure, Airport arrival) {

        /**
         * Returns a string representation of the ApiData object.
         *
         * @return The string representation of the ApiData object.
         */
        @Override
        public String toString() {
            return "Flight Details:\n" +
                    "Name: " + name + "\n" +
                    departure.IATA + " -> " + arrival.IATA;
        }
    }

    /**
     * Represents an airport with its IATA code and municipality name.
     */
    public record Airport(String IATA, String municipalityName) {}
}
