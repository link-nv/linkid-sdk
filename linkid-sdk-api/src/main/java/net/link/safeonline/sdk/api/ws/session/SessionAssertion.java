/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.session;

import java.util.Date;
import java.util.Map;


/**
 * <h2>{@link SessionAssertion}</h2>
 * <p/>
 * <p>
 * [description / usage].
 * </p>
 * <p/>
 * <p>
 * <i>Apr 3, 2009</i>
 * </p>
 *
 * @author wvdhaute
 */
public class SessionAssertion {

    private final String subject;

    private final String applicationPool;

    private final Map<Date, String> authentications;

    public SessionAssertion(final String subject, final String applicationPool, final Map<Date, String> authentications) {

        this.subject = subject;
        this.applicationPool = applicationPool;
        this.authentications = authentications;
    }

    public String getSubject() {

        return subject;
    }

    public String getApplicationPool() {

        return applicationPool;
    }

    public Map<Date, String> getAuthentications() {

        return authentications;
    }
}
