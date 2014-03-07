/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.oauth2.library.messages;

import java.util.List;


/**
 * <p/>
 * Date: 07/05/12
 * Time: 11:24
 *
 * @author sgdesmet
 */
public class ValidationResponse implements ResponseMessage {

    protected String audience;

    protected Long expiresIn;

    protected List<String> scope;

    protected String userId;

    public String getAudience() {

        return audience;
    }

    public void setAudience(final String audience) {

        this.audience = audience;
    }

    public Long getExpiresIn() {

        return expiresIn;
    }

    public void setExpiresIn(final Long expiresIn) {

        this.expiresIn = expiresIn;
    }

    public List<String> getScope() {

        return scope;
    }

    public void setScope(final List<String> scope) {

        this.scope = scope;
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(final String userId) {

        this.userId = userId;
    }
}
