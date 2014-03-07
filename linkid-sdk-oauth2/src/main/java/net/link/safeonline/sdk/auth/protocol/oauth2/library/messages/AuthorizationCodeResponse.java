/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.oauth2.library.messages;

/**
 * Authorization code response
 * <p/>
 * Date: 20/03/12
 * Time: 14:25
 *
 * @author sgdesmet
 */
public class AuthorizationCodeResponse implements ResponseMessage {

    protected String code; //required
    protected String state; //required if included in request

    public String getCode() {

        return code;
    }

    public void setCode(final String code) {

        this.code = code;
    }

    public String getState() {

        return state;
    }

    public void setState(final String state) {

        this.state = state;
    }

    public AuthorizationCodeResponse(final String code) {

        this.code = code;
    }

    public AuthorizationCodeResponse() {

    }
}
