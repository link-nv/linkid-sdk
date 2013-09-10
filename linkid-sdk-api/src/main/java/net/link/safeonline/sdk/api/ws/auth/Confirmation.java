/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.api.ws.auth;

import java.util.HashMap;
import java.util.Map;


/**
 * <h2>{@link Confirmation}</h2>
 * <p/>
 * <p>
 * [description / usage].
 * </p>
 * <p/>
 * <p>
 * <i>Jan 13, 2008</i>
 * </p>
 *
 * @author wvdhaute
 */
public enum Confirmation {

    CONFIRM( "confirm" ),
    REJECT( "reject" );

    private final String value;

    private static final Map<String, Confirmation> confirmationMap = new HashMap<String, Confirmation>();

    static {
        Confirmation[] confirmations = Confirmation.values();
        for (Confirmation confirmation : confirmations) {
            confirmationMap.put( confirmation.getValue(), confirmation );
        }
    }

    Confirmation(String value) {

        this.value = value;
    }

    public String getValue() {

        return value;
    }

    @Override
    public String toString() {

        return value;
    }

    public static Confirmation getConfirmation(String confirmationValue) {

        Confirmation confirmation = confirmationMap.get( confirmationValue );
        if (null == confirmation)
            throw new IllegalArgumentException( String.format( "unknown confirmation: %s", confirmationValue ) );
        return confirmation;
    }
}
