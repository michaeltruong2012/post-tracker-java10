package au.com.posttracker.services;

/**
 * @author Michael Truong
 */
public interface ShipmentTrackingService {
    Shipment track(String trackingId);
}
