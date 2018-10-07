package au.com.posttracker.services;

import java.text.MessageFormat;

/**
 * Exception being raised when there is an error while tracking a shipment.
 *
 * @author Michael Truong
 */
public class ShipmentTrackingException extends RuntimeException {

    public ShipmentTrackingException(String message, Object... params) {
        super(MessageFormat.format(message, params));
    }

    public ShipmentTrackingException(Throwable cause, String message, Object... params) {
        super(MessageFormat.format(message, params), cause);
    }
}
