/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

import static com.google.common.base.Preconditions.*;

import net.link.util.common.CertificateChain;


/**
 * <h2>{@link LogoutProtocolRequestContext}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>08 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class LogoutProtocolRequestContext extends ProtocolRequestContext {

    private final String           userId;
    private       CertificateChain certificateChain;

    /**
     * @param id              ID of the Logout Request.
     * @param issuer          Issuer of the Logout Request.
     * @param protocolHandler Logout Protocol Handler.
     * @param target          Target URL of the Logout Request.
     * @param userId          The user that should be logged out.
     */
    public LogoutProtocolRequestContext(String id, String issuer, ProtocolHandler protocolHandler, String target, String userId) {

        super( id, issuer, protocolHandler, target );
        this.userId = userId;
    }

    /**
     * @return The user that should be logged out.
     */
    public String getUserId() {

        return checkNotNull( userId, "User Id not set for %s", this );
    }

    /**
     * @return optional certificate chain embedded in the logout request's signature.
     */
    public CertificateChain getCertificateChain() {

        return certificateChain;
    }

    /**
     * @param certificateChain optional certificate chain embedded in the logout request's signature.
     */
    public void setCertificateChain(final CertificateChain certificateChain) {

        this.certificateChain = certificateChain;
    }
}
