package net.link.safeonline.sdk.api.reporting;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.sdk.api.parking.LinkIDParkingSession;


/**
 * Created by wvdhaute
 * Date: 19/11/15
 * Time: 10:45
 */
public class LinkIDParkingReport implements Serializable {

    private final long                       total;
    private final List<LinkIDParkingSession> parkingSessions;

    public LinkIDParkingReport(final long total, final List<LinkIDParkingSession> parkingSessions) {

        this.total = total;
        this.parkingSessions = parkingSessions;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDParkingReport{" +
               "total=" + total +
               ", parkingSessions=" + parkingSessions +
               '}';
    }

    // Accessors

    public long getTotal() {

        return total;
    }

    public List<LinkIDParkingSession> getParkingSessions() {

        return parkingSessions;
    }
}
