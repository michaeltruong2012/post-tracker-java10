package au.com.posttracker.services;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Shipment {

    private String trackingId;
    private String consignmentId;
    private String status;

    private List<ShipmentItem> items = new LinkedList<>();

    public String getTrackingId() {
        return trackingId;
    }

    public Shipment withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

    public String getConsignmentId() {
        return consignmentId;
    }

    public void setConsignmentId(String consignmentId) {
        this.consignmentId = consignmentId;
    }

    public List<ShipmentItem> getItems() {
        return items;
    }

    public void setItems(List<ShipmentItem> items) {
        this.items = items;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Shipment{" +
                "trackingId='" + trackingId + '\'' +
                ", consignmentId='" + consignmentId + '\'' +
                ", status='" + status + '\'' +
                ", items.size=" + Optional.ofNullable(items).map(List::size).orElse(0) +
                '}';
    }
}
