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
