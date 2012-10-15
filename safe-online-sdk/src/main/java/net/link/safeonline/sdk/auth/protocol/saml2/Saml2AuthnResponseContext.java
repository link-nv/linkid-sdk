/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Copyright 2005-2006 Frank Cornelis.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.util.common.CertificateChain;
import org.opensaml.saml2.core.*;


/**
 * Holds a SAML {@link StatusResponseType} object and the {@link X509Certificate} chain embedded in the Signature on the response if any.
 * If
 * Binding was HTTP-Redirect the chain is <code>null</code>
 *
 * @author Wim Vandenhaute
 */
public class Saml2AuthnResponseContext {

    private final Response                           response;
    private final CertificateChain                   certificateChain;
    private final String                             userId;
    private final String                             applicationName;
    private final List<String>                       authenticatedDevices;
    private final Map<String, List<AttributeSDK<?>>> attributes;

    public Saml2AuthnResponseContext(final Response response, final CertificateChain certificateChain, final String userId,
                                     final String applicationName, final List<String> authenticatedDevices,
                                     final Map<String, List<AttributeSDK<?>>> attributes) {

        this.response = response;
        this.certificateChain = certificateChain;
        this.userId = userId;
        this.applicationName = applicationName;
        this.authenticatedDevices = authenticatedDevices;
        this.attributes = attributes;
    }

    /**
     * @return the SAML v2.0 authentication response
     */
    public Response getResponse() {

        return response;
    }

    /**
     * @return the (optional) embedded certificate chain in the signed response.
     */
    public CertificateChain getCertificateChain() {

        return certificateChain;
    }

    public String getUserId() {

        return userId;
    }

    public String getApplicationName() {

        return applicationName;
    }

    public List<String> getAuthenticatedDevices() {

        return authenticatedDevices;
    }

    public Map<String, List<AttributeSDK<?>>> getAttributes() {

        return attributes;
    }

    public boolean isSuccess() {

        return response.getStatus().getStatusCode().getValue().equals( StatusCode.SUCCESS_URI );
    }
}
