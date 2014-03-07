/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects;

import java.io.Serializable;
import java.util.List;


/**
 * <p/>
 * Date: 19/03/12
 * Time: 14:39
 *
 * @author sgdesmet
 */
public class ClientConfiguration implements Serializable {

    protected boolean        confidential;
    protected String         clientId;
    protected String         clientSecret;
    protected List<String>   redirectUris;
    protected List<String>   configuredScope;
    protected List<FlowType> allowedFlows;

    /**
     * Include a refresh token for authorization grant and password credential flows?
     */
    protected boolean includeRefreshToken;

    protected long defaultCodeLifeTime;
    protected long defaultAccessTokenLifeTime;
    protected long defaultRefreshTokenLifeTime;


    public static enum FlowType {AUTHORIZATION, IMPLICIT, RESOURCE_CREDENTIALS, CLIENT_CREDENTIALS}

    public ClientConfiguration(final String clientId) {

        this.clientId = clientId;
    }

    public boolean isConfidential() {

        return confidential;
    }

    public void setConfidential(final boolean confidential) {

        this.confidential = confidential;
    }

    public String getClientId() {

        return clientId;
    }

    public void setClientId(final String clientId) {

        this.clientId = clientId;
    }

    public String getClientSecret() {

        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {

        this.clientSecret = clientSecret;
    }

    public List<String> getRedirectUris() {

        return redirectUris;
    }

    public void setRedirectUris(final List<String> redirectUris) {

        this.redirectUris = redirectUris;
    }

    public List<String> getConfiguredScope() {

        return configuredScope;
    }

    public void setConfiguredScope(final List<String> configuredScope) {

        this.configuredScope = configuredScope;
    }

    public List<FlowType> getAllowedFlows() {

        return allowedFlows;
    }

    public void setAllowedFlows(final List<FlowType> allowedFlows) {

        this.allowedFlows = allowedFlows;
    }

    public long getDefaultCodeLifeTime() {

        return defaultCodeLifeTime;
    }

    public void setDefaultCodeLifeTime(final long defaultCodeLifeTime) {

        this.defaultCodeLifeTime = defaultCodeLifeTime;
    }

    public long getDefaultAccessTokenLifeTime() {

        return defaultAccessTokenLifeTime;
    }

    public void setDefaultAccessTokenLifeTime(final long defaultAccessTokenLifeTime) {

        this.defaultAccessTokenLifeTime = defaultAccessTokenLifeTime;
    }

    public long getDefaultRefreshTokenLifeTime() {

        return defaultRefreshTokenLifeTime;
    }

    public void setDefaultRefreshTokenLifeTime(final long defaultRefreshTokenLifeTime) {

        this.defaultRefreshTokenLifeTime = defaultRefreshTokenLifeTime;
    }

    public boolean isIncludeRefreshToken() {

        return includeRefreshToken;
    }

    public void setIncludeRefreshToken(final boolean includeRefreshToken) {

        this.includeRefreshToken = includeRefreshToken;
    }
}
