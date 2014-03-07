/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;
import java.util.Date;


@SuppressWarnings("UnusedDeclaration")
public class LinkIDHistory implements Serializable {

    private final Date   when;
    private final String message;

    public LinkIDHistory(final Date when, final String message) {

        this.when = when;
        this.message = message;
    }

    public Date getWhen() {

        return when;
    }

    public String getMessage() {

        return message;
    }
}
