/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.auth;

public enum LinkIDPaymentType {

    VISA,
    MC,
    MAESTRO,
    NEW,
    KLARNA;

    public static LinkIDPaymentType parse(final String paymentCodeTypeString) {

        if (null == paymentCodeTypeString || 0 == paymentCodeTypeString.trim().length())
            return null;

        for (LinkIDPaymentType paymentCodeType : LinkIDPaymentType.values()) {
            if (paymentCodeType.name().equals( paymentCodeTypeString )) {
                return paymentCodeType;
            }
        }

        return null;
    }
}
