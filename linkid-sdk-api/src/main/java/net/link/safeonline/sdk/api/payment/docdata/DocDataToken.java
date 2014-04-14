/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.payment.docdata;

import java.io.Serializable;


public class DocDataToken implements Serializable {

    private final String           attributeId;
    private final String           tokenId;
    private final DocDataTokenType tokenType;
    private final String           prettyPrint;
    private final int              priority;

    public DocDataToken(final String attributeId, final String tokenId, final DocDataTokenType tokenType, final String prettyPrint, final int priority) {

        this.attributeId = attributeId;
        this.tokenId = tokenId;
        this.tokenType = tokenType;
        this.prettyPrint = prettyPrint;
        this.priority = priority;
    }

    public String getAttributeId() {

        return attributeId;
    }

    public String getTokenId() {

        return tokenId;
    }

    public DocDataTokenType getTokenType() {

        return tokenType;
    }

    public String getPrettyPrint() {

        return prettyPrint;
    }

    public int getPriority() {

        return priority;
    }
}
