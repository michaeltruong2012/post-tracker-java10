package au.com.posttracker.cli;

import au.com.posttracker.services.Shipment;
import au.com.posttracker.services.ShipmentItem;
import au.com.posttracker.services.ShipmentTrackingService;
import au.com.posttracker.services.spi.auspost.AuspostShipmentTrackingService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * @author Michael Truong
 */
public class Application {

    private ShipmentTrackingService trackingService = new AuspostShipmentTrackingService();

    public static void main(String[] args) {
        Options options = new Options()
                .addOption("tid", true, "Tracking ID")
                .addOption("h", false, "Show this help");

        CommandLineParser cliParser = new DefaultParser();
        CommandLine cli = null;
        try {
            cli = cliParser.parse(options, args);
        } catch (ParseException e) {
            System.err.printf("ERROR - Cannot parse commandline arguments. Details: %s\n", e.getMessage());
            System.exit(400);
        }

        if (cli.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("posttracker", options);
        } else if (cli.hasOption("tid")) {
            var trackingId = cli.getOptionValue("tid");
            System.out.println(new Application().getTrackingInfo(trackingId));
        } else {
            System.err.println("ERROR - No argument is provided");
        }
    }

    private String getTrackingInfo(String trackingId) {
        Shipment details = trackingService.track(trackingId);
        return printShipmentDetails(trackingId, details);
    }

    private String printShipmentDetails(String trackingId, Shipment shipment) {
        return "SHIPMENT DETAILS >> TRACKING ID: " + trackingId + '\n' +
                "--------------------------------" + '\n' +
                Optional.ofNullable(shipment).map(this::printShipment).orElse("Unknown") + '\n' +
                "--------------------------------";
    }

    private String printShipment(Shipment shipment) {
        var txt = new StringBuilder()
                .append("Tracking ID    : ").append(shipment.getTrackingId()).append("\n")
                .append("Consignment ID : ").append(shipment.getConsignmentId()).append("\n")
                .append("Status         : ").append(shipment.getStatus()).append("\n");

        txt.append("Items [").append("\n");
        shipment.getItems().forEach(item -> txt.append(printShipmentItem(item)).append("\n"));
        txt.append("]\n");
        txt.append("(Found ").append(shipment.getItems().size()).append(" shipment item(s))");

        return txt.toString();
    }

    private String printShipmentItem(ShipmentItem item) {
        String deliveryDateTime = Optional.ofNullable(item.getDeliveryTimestamp())
                .map(ts -> LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault()))
                .map(dt -> " (updated on " + dt.toString() + ")")
                .orElse("");

        return "" +
                "  " + "=> " + "Item ID     : " + item.getItemId() + "\n" +
                "  " + "   " + "Status      : " + item.getDeliveryStatus() + deliveryDateTime + "\n" +
                "  " + "   " + "Progress (%): " + item.getProgressPercentage() + "\n" +
                "  " + "   " + "Journey     : " + item.getSourceCountryName() + " -> " + item.getDestinationCountryName();
    }
}
