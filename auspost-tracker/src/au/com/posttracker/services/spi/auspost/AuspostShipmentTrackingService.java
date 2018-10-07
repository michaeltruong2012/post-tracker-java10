package au.com.posttracker.services.spi.auspost;

import au.com.posttracker.services.Shipment;
import au.com.posttracker.services.ShipmentItem;
import au.com.posttracker.services.ShipmentTrackingException;
import au.com.posttracker.services.ShipmentTrackingService;
import com.google.gson.Gson;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link au.com.posttracker.services.ShipmentTrackingService} which invokes REST service to AUSPOST to get shipment details.
 *
 * @author Michael Truong
 */
public class AuspostShipmentTrackingService implements ShipmentTrackingService {

    private static final String AUSPOST_ENDPOINT = "https://digitalapi.auspost.com.au/shipmentsgatewayapi/watchlist/shipments";
    private static final String AUSPOST_API_KEY = "d11f9456-11c3-456d-9f6d-f7449cb9af8e";

    @Override
    public Shipment track(String trackingId) {
        HttpRequest request = createRequest(trackingId);
        HttpResponse<String> response = sendRequest(trackingId, request);

        if (response.statusCode() != 200) {
            System.out.println("FAILURE");
            throw new ShipmentTrackingException(
                    "Error while getting shipment details for tracking ID ''{0}''; Unsuccessful response (code: {1})",
                    trackingId, response.statusCode());
        }
        return ofNullable(response.body())
                .map(String::trim)
                .map(this::toShipments)
                .filter(results -> !results.isEmpty())
                .map(results -> results.get(0).withTrackingId(trackingId))
                .orElse(null);
    }

    private HttpResponse<String> sendRequest(String trackingId, HttpRequest request) {
        HttpResponse<String> response;
        try {
            System.out.print("Fetching shipment details ... ");
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandler.asString());
            System.out.println("SUCCESS");

        } catch (IOException e) {
            System.out.println("ERROR");
            throw new ShipmentTrackingException(e,
                    "Error while getting shipment details for tracking ID ''{0}''; IO errors",
                    trackingId);

        } catch (InterruptedException e) {
            System.out.println("ERROR");
            throw new ShipmentTrackingException(e,
                    "Error while getting shipment details for tracking ID ''{0}''; times out!",
                    trackingId);

        } catch (RuntimeException e) {
            System.out.println("ERROR");
            throw e;
        }
        return response;
    }

    private HttpRequest createRequest(String trackingId) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(AUSPOST_ENDPOINT + "?trackingIds=" + trackingId))
                    .GET()
                    .header("api-key", AUSPOST_API_KEY)
                    .timeout(Duration.ofSeconds(60))
                    .build();
        } catch (URISyntaxException e) {
            throw new ShipmentTrackingException(e,
                    "Error while getting shipment details for tracking ID ''{0}''; invalid endpoint URL {1}",
                    trackingId, AUSPOST_ENDPOINT);
        }
        return request;
    }

    private List<Shipment> toShipments(String responseBodyJson) {
        List<?> responseBody = new Gson().fromJson(responseBodyJson, List.class);
        return ofNullable(responseBody)
                .filter(l -> !l.isEmpty())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(item -> (Map) item)
                .map(this::createShipment)
                .collect(Collectors.toUnmodifiableList());
    }

    private Shipment createShipment(Map item) {
        var shipment = new Shipment();

        var shipmentEle = (Map) item.get("shipment");
        shipment.setConsignmentId((String) shipmentEle.get("consignmentId"));
        shipment.setStatus((String) shipmentEle.get("status"));

        List<Map<String, Object>> articles = getValueAsList(shipmentEle, "articles");
        var items = articles.stream()
                .map(this::toShipmentItem)
                .collect(Collectors.toList());
        shipment.setItems(items);

        return shipment;
    }

    private ShipmentItem toShipmentItem(Map articleEle) {
        var item = new ShipmentItem();

        item.setItemId((String) articleEle.get("articleId"));

        Map<String, Object> deliveryStatusEle = getValueAsMap(articleEle, "status");
        item.setDeliveryStatus((String) deliveryStatusEle.getOrDefault("statusAttributeValue", "Unknown"));
        item.setDeliveryTimestamp(ofNullable((Number) deliveryStatusEle.get("statusModificationDateTime")).map(Number::longValue).orElse(null));

        getValueAsList(articleEle, "details")
                .stream()
                .filter(m -> Objects.equals(m.get("articleId"), item.getItemId()))
                .findFirst()
                .ifPresent(ele -> {
                    item.setShipmentType((String) ele.get("articleType"));
                    item.setDestinationCountryName((String) getValueAsMap(ele, "address").get("countryName"));
                    item.setSourceCountryName((String) getValueAsMap(ele, "fromAddress").get("countryName"));

                    List<Map<String, Object>> milestoneEles = getValueAsList(ele, "milestones");

                    var completed = milestoneEles.stream()
                            .anyMatch(i -> {
                                Number percentage = (Number) i.get("progressPercentage");
                                return percentage != null && percentage.intValue() == 100 && Objects.equals(i.get("status"), "Completed");
                            });
                    if (completed) {
                        item.setProgressPercentage(100);
                    } else {
                        milestoneEles
                                .stream()
                                .filter(i -> Objects.equals(i.get("status"), "Current"))
                                .findFirst()
                                .ifPresent(i -> item.setProgressPercentage(
                                        ofNullable((Number) i.get("progressPercentage"))
                                                .map(Number::intValue)
                                                .orElse(null)));
                    }
                });

        return item;
    }

    private static Map<String, Object> getValueAsMap(Map map, Object key) {
        return ofNullable(map.get(key))
                .filter(v -> v instanceof Map)
                .map(v -> (Map<String, Object>) v)
                .orElseGet(Collections::emptyMap);
    }

    private static List<Map<String, Object>> getValueAsList(Map map, Object key) {
        return ofNullable(map.get(key))
                .filter(v -> v instanceof List)
                .map(v -> (List<Map<String, Object>>) v)
                .orElseGet(Collections::emptyList);
    }
}
