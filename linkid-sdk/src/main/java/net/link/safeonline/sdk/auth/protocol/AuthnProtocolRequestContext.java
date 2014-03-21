/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

/**
 * <h2>{@link AuthnProtocolRequestContext}<br>
 * <sub>[in short].</sub></h2>
 * <p/>
 * <p>
 * <i>08 17, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public class AuthnProtocolRequestContext extends ProtocolRequestContext {

    private final boolean mobileForceRegistration;

    public AuthnProtocolRequestContext(final String id, final String issuer, final ProtocolHandler protocolHandler, final String target,
                                       final boolean mobileForceRegistration) {

        super( id, issuer, protocolHandler, target );

        this.mobileForceRegistration = mobileForceRegistration;
    }

    public boolean isMobileForceRegistration() {

        return mobileForceRegistration;
    }
}
