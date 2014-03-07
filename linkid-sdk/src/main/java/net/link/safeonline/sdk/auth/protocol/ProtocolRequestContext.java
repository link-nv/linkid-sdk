/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

import static com.google.common.base.Preconditions.checkNotNull;

import net.link.safeonline.sdk.api.auth.LoginMode;


/**
 * <h2>{@link ProtocolRequestContext}<br>
 * <sub>[in short].</sub></h2>
 * <p/>
 * <p>
 * <i>08 19, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public class ProtocolRequestContext extends ProtocolContext {

    private final String          target;
    private final String          issuer;
    private final ProtocolHandler protocolHandler;
    private       LoginMode       loginMode;

    /**
     * @param id              A unique ID that will match the response to this request.
     * @param issuer          The application the issued the request.
     * @param protocolHandler The protocol handler and its configuration that was used for this request and will be used to validate the response.
     * @param target          The URL to redirect to after handling the response to this request.
     */
    public ProtocolRequestContext(String id, String issuer, ProtocolHandler protocolHandler, String target) {

        super( id );
        this.target = target;
        this.issuer = issuer;
        this.protocolHandler = protocolHandler;
    }

    /**
     * @return The protocol handler and its configuration that was used for this request and will be used to validate the response.
     */
    public ProtocolHandler getProtocolHandler() {

        return checkNotNull( protocolHandler, "Protocol handler not set for %s", this );
    }

    /**
     * @return The application that issued the request.
     */
    public String getIssuer() {

        return checkNotNull( issuer, "Issuer not set for %s", this );
    }

    /**
     * @return The URL to redirect to after handling the response to this request.
     */
    public String getTarget() {

        return checkNotNull( target, "Target not set for %s", this );
    }

    @Override
    public String toString() {

        return String.format( "{%s: %s, issuer: %s, handler: %s, target: %s}", getClass().getSimpleName(), getId(), issuer, protocolHandler, target );
    }

    public LoginMode getLoginMode() {

        return loginMode;
    }

    public void setLoginMode(final LoginMode loginMode) {

        this.loginMode = loginMode;
    }
}
