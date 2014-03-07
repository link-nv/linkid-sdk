/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.oauth2.library.messages;

/**
 * <p/>
 * Date: 07/05/12
 * Time: 11:22
 *
 * @author sgdesmet
 */
public class ValidationRequest implements RequestMessage {

    protected String accessToken;

    protected String clientId;

    protected String clientSecret;

    public String getAccessToken() {

        return accessToken;
    }

    public void setAccessToken(final String accessToken) {

        this.accessToken = accessToken;
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
}
