package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;


@SuppressWarnings("UnusedDeclaration")
public class LinkIDPaymentMethod implements Serializable {

    private final String            tokenId;
    private final LinkIDPaymentType tokenType;
    private final String            prettyPrint;
    private final int               priority;

    public LinkIDPaymentMethod(final String tokenId, final LinkIDPaymentType tokenType, final String prettyPrint, final int priority) {

        this.tokenId = tokenId;
        this.tokenType = tokenType;
        this.prettyPrint = prettyPrint;
        this.priority = priority;
    }

    public String getTokenId() {

        return tokenId;
    }

    public LinkIDPaymentType getTokenType() {

        return tokenType;
    }

    public String getPrettyPrint() {

        return prettyPrint;
    }

    public int getPriority() {

        return priority;
    }
}
