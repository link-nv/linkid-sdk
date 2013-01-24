package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;
import java.util.Date;


@SuppressWarnings("UnusedDeclaration")
public class LinkIDHistory implements Serializable {

    private final Date   date;
    private final String message;

    public LinkIDHistory(final Date date, final String message) {

        this.date = date;
        this.message = message;
    }

    public Date getDate() {

        return date;
    }

    public String getMessage() {

        return message;
    }
}
