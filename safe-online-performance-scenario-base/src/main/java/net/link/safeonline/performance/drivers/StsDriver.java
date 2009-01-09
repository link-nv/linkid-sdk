/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.drivers;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;

import org.w3c.dom.Element;


/**
 * <h2>{@link StsDriver}<br>
 * <sub>Driver for the Security Token validation service.</sub></h2>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class StsDriver extends ProfileDriver {

    public static final String NAME        = "Security Token Service Driver";
    public static final String DESCRIPTION = "<b>Security Token Service Driver:</b><br>"
                                                   + "Checks the validity of a SAML token and causes an exception if it is not valid.";


    public StsDriver(ExecutionEntity execution, ScenarioTimingEntity agentTime) {

        super(NAME, execution, agentTime);
    }

    /**
     * Validate the given SAML token.
     * 
     * @param applicationKey
     *            The certificate of the application making the request. This identifies the application and gives the request the
     *            application's authority.
     * @param token
     *            The SAML token that needs to be validated.
     * @param trustDomain
     *            The trusted party that released the certificate of the token.
     */
    public void validate(PrivateKeyEntry applicationKey, Element token, TrustDomainType trustDomain) {

        if (!(applicationKey.getCertificate() instanceof X509Certificate))
            throw new IllegalArgumentException("The certificate in the keystore needs to be of X509 format.");

        try {
            SecurityTokenServiceClientImpl service = new SecurityTokenServiceClientImpl(getHost(),
                    (X509Certificate) applicationKey.getCertificate(), applicationKey.getPrivateKey());

            try {
                service.validate(token, trustDomain);
            } finally {
                report(service);
            }
        }

        catch (Throwable error) {
            throw report(error);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {

        return DESCRIPTION;
    }
}
