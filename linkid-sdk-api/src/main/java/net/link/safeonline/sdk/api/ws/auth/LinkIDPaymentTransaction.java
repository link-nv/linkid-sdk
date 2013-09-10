package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;
import java.util.Date;
import net.link.safeonline.sdk.api.payment.Currency;


@SuppressWarnings("UnusedDeclaration")
public class LinkIDPaymentTransaction implements Serializable {

    private final String  transactionId;
    private final boolean payed;

    private final double   amount;
    private final Currency currency;
    private final String   encodedDescription;
    private final String   profile;

    private final Date              created;
    private final long              applicationId;
    private final String            applicationName;
    private final String            tokenId;
    private final String            tokenPrettyPrint;
    private final LinkIDPaymentType tokenType;

    public LinkIDPaymentTransaction(final String transactionId, final boolean payed, final double amount, final Currency currency,
                                    final String encodedDescription, final String profile, final Date created, final long applicationId,
                                    final String applicationName, final String tokenId, final String tokenPrettyPrint, final LinkIDPaymentType tokenType) {

        this.transactionId = transactionId;

        this.payed = payed;
        this.amount = amount;
        this.currency = currency;
        this.encodedDescription = encodedDescription;
        this.profile = profile;
        this.created = created;
        this.applicationId = applicationId;
        this.applicationName = applicationName;
        this.tokenId = tokenId;
        this.tokenPrettyPrint = tokenPrettyPrint;
        this.tokenType = tokenType;
    }

    public String getTransactionId() {

        return transactionId;
    }

    public boolean isPayed() {

        return payed;
    }

    public double getAmount() {

        return amount;
    }

    public Currency getCurrency() {

        return currency;
    }

    public String getEncodedDescription() {

        return encodedDescription;
    }

    public String getProfile() {

        return profile;
    }

    public Date getCreated() {

        return created;
    }

    public long getApplicationId() {

        return applicationId;
    }

    public String getTokenId() {

        return tokenId;
    }

    public String getTokenPrettyPrint() {

        return tokenPrettyPrint;
    }

    public String getApplicationName() {

        return applicationName;
    }

    public LinkIDPaymentType getTokenType() {

        return tokenType;
    }
}
