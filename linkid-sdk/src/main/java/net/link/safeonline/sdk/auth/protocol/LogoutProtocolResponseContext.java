/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

import net.link.util.common.CertificateChain;


/**
 * <h2>{@link LogoutProtocolResponseContext}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>08 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class LogoutProtocolResponseContext extends ProtocolResponseContext {

    private final boolean success;

    /**
     * @param request          Logout Request this response is a response to.
     * @param id               Logout Response ID
     * @param success          Whether the logout response reports a successful and non-partial (complete) single logout.
     * @param certificateChain Optional certificate chain if protocol response was signed and contained the chain embedded in the
     *                         signature.
     */
    public LogoutProtocolResponseContext(LogoutProtocolRequestContext request, String id, boolean success, CertificateChain certificateChain) {

        super( request, id, certificateChain );

        this.success = success;
    }

    @Override
    public LogoutProtocolRequestContext getRequest() {

        return (LogoutProtocolRequestContext) super.getRequest();
    }

    /**
     * @return Whether the logout response reports a successful and non-partial (complete) single logout.
     */
    public boolean isSuccess() {

        return success;
    }
}
