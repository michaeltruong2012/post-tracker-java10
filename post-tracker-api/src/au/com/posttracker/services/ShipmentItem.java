package au.com.posttracker.services;

/**
 * @author Michael Truong
 */
public class ShipmentItem {
    private String itemId;
    private String shipmentType;

    private String sourceCountryName;
    private String destinationCountryName;

    private String deliveryStatus;
    private Long deliveryTimestamp;

    private Integer progressPercentage;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getShipmentType() {
        return shipmentType;
    }

    public void setShipmentType(String shipmentType) {
        this.shipmentType = shipmentType;
    }

    public String getSourceCountryName() {
        return sourceCountryName;
    }

    public void setSourceCountryName(String sourceCountryName) {
        this.sourceCountryName = sourceCountryName;
    }

    public String getDestinationCountryName() {
        return destinationCountryName;
    }

    public void setDestinationCountryName(String destinationCountryName) {
        this.destinationCountryName = destinationCountryName;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public Long getDeliveryTimestamp() {
        return deliveryTimestamp;
    }

    public void setDeliveryTimestamp(Long deliveryTimestamp) {
        this.deliveryTimestamp = deliveryTimestamp;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    @Override
    public String toString() {
        return "ShipmentItem{" +
                "itemId='" + itemId + '\'' +
                ", shipmentType='" + shipmentType + '\'' +
                ", sourceCountryName='" + sourceCountryName + '\'' +
                ", destinationCountryName='" + destinationCountryName + '\'' +
                ", deliveryStatus='" + deliveryStatus + '\'' +
                ", deliveryTimestamp=" + deliveryTimestamp +
                ", progressPercentage=" + progressPercentage +
                '}';
    }
}
