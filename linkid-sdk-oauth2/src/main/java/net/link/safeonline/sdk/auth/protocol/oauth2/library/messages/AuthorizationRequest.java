/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.oauth2.library.messages;

import java.util.List;


/**
 * An Oauth2 Authorization Code request message (for either Authorization Code flow or implicit grant flow).
 * <p/>
 * Date: 15/03/12
 * Time: 15:53
 *
 * @author sgdesmet
 */
public class AuthorizationRequest implements RequestMessage {

    protected ResponseType responseType; //required
    protected String       clientId; //required
    protected String       clientSecret;
    protected String       redirectUri;
    protected List<String> scope;
    protected String       state;

    public AuthorizationRequest(final ResponseType responseType, final String clientId) {

        this.responseType = responseType;
        this.clientId = clientId;
    }

    public AuthorizationRequest() {

    }

    public ResponseType getResponseType() {

        return responseType;
    }

    public void setResponseType(final ResponseType responseType) {

        this.responseType = responseType;
    }

    public String getClientId() {

        return clientId;
    }

    public void setClientId(final String clientId) {

        this.clientId = clientId;
    }

    public String getRedirectUri() {

        return redirectUri;
    }

    public void setRedirectUri(final String redirectUri) {

        this.redirectUri = redirectUri;
    }

    public List<String> getScope() {

        return scope;
    }

    public void setScope(final List<String> scope) {

        this.scope = scope;
    }

    public String getState() {

        return state;
    }

    public void setState(final String state) {

        this.state = state;
    }

    public String getClientSecret() {

        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {

        this.clientSecret = clientSecret;
    }

    @Override
    public String toString() {

        return "AuthorizationRequest{" +
               "responseType=" + responseType +
               ", clientId='" + clientId + '\'' +
               ", clientSecret='" + clientSecret + '\'' +
               ", redirectUri='" + redirectUri + '\'' +
               ", scope=" + scope +
               ", state='" + state + '\'' +
               '}';
    }
}
