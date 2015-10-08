package net.link.safeonline.sdk.api.ws.linkid.ltqr;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 28/07/15
 * Time: 11:36
 */
public class LinkIDLTQRPollingConfiguration implements Serializable {

    private final int pollAttempts;
    private final int pollInterval;
    //
    private final int paymentPollAttempts;
    private final int paymentPollInterval;

    public LinkIDLTQRPollingConfiguration(final int pollAttempts, final int pollInterval, final int paymentPollAttempts, final int paymentPollInterval) {

        this.pollAttempts = pollAttempts;
        this.pollInterval = pollInterval;
        this.paymentPollAttempts = paymentPollAttempts;
        this.paymentPollInterval = paymentPollInterval;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDLTQRPollingConfiguration{" +
               "pollAttempts=" + pollAttempts +
               ", pollInterval=" + pollInterval +
               ", paymentPollAttempts=" + paymentPollAttempts +
               ", paymentPollInterval=" + paymentPollInterval +
               '}';
    }

    // Accessors

    public int getPollAttempts() {

        return pollAttempts;
    }

    public int getPollInterval() {

        return pollInterval;
    }

    public int getPaymentPollAttempts() {

        return paymentPollAttempts;
    }

    public int getPaymentPollInterval() {

        return paymentPollInterval;
    }
}
