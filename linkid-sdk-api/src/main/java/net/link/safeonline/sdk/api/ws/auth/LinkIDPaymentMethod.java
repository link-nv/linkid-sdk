/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;


@SuppressWarnings("UnusedDeclaration")
public class LinkIDPaymentMethod implements Serializable {

    private final String            attributeId;
    private final String            tokenId;
    private final LinkIDPaymentType tokenType;
    private final String            prettyPrint;
    private final int               priority;
    private final boolean           removable;

    public LinkIDPaymentMethod(final String attributeId, final String tokenId, final LinkIDPaymentType tokenType, final String prettyPrint, final int priority,
                               final boolean removable) {

        this.attributeId = attributeId;
        this.tokenId = tokenId;
        this.tokenType = tokenType;
        this.prettyPrint = prettyPrint;
        this.priority = priority;
        this.removable = removable;
    }

    public String getAttributeId() {

        return attributeId;
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

    public boolean isRemovable() {

        return removable;
    }
}
