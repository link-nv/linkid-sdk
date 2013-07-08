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
    private final String   description;
    private final String   profile;

    private final Date   created;
    private final long   applicationId;
    private       String tokenId;
    private       String tokenPrettyPrint;

    public LinkIDPaymentTransaction(final String transactionId, final boolean payed, final double amount, final Currency currency, final String description,
                                    final String profile, final Date created, final long applicationId, final String tokenId, final String tokenPrettyPrint) {

        this.transactionId = transactionId;

        this.payed = payed;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.profile = profile;
        this.created = created;
        this.applicationId = applicationId;
        this.tokenId = tokenId;
        this.tokenPrettyPrint = tokenPrettyPrint;
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

    public String getDescription() {

        return description;
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

    public void setTokenId(final String tokenId) {

        this.tokenId = tokenId;
    }

    public String getTokenPrettyPrint() {

        return tokenPrettyPrint;
    }

    public void setTokenPrettyPrint(final String tokenPrettyPrint) {

        this.tokenPrettyPrint = tokenPrettyPrint;
    }
}
