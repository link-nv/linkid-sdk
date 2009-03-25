/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.auth.saml2.request;

import java.security.KeyPair;
import java.util.Collections;
import java.util.Set;

import net.link.safeonline.saml.common.Challenge;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;


/**
 * <h2>{@link DeviceAuthnRequestFactory}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 25, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceAuthnRequestFactory {

    private DeviceAuthnRequestFactory() {

        // empty
    }

    /**
     * Create a SAML v2.0 Authentication request sent to a remote device provider.
     */
    public static String createDeviceAuthnRequest(String issuerName, String applicationName, String applicationFriendlyName,
                                                  KeyPair signerKeyPair, String assertionConsumerServiceURL, String destinationURL,
                                                  Challenge<String> challenge, Set<String> devices) {

        if (null == applicationName)
            throw new IllegalArgumentException("application name should not be null");

        return AuthnRequestFactory.createAuthnRequest(issuerName, Collections.singletonList(applicationName), applicationFriendlyName,
                signerKeyPair, assertionConsumerServiceURL, destinationURL, challenge, devices, false);
    }

}
