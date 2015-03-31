/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.payment;

public enum LinkIDCurrency {

    EUR;

    public static LinkIDCurrency parse(final String currencyString) {

        for (LinkIDCurrency currency : LinkIDCurrency.values()) {

            if (currency.name().equals( currencyString.toUpperCase() )) {
                return currency;
            }
        }

        throw new RuntimeException( String.format( "Unsupported currency %s!", currencyString ) );
    }
}
