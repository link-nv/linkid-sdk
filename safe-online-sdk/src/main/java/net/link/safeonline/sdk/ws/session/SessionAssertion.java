/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws.session;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.lin_k.safe_online.session.AssertionType;
import net.lin_k.safe_online.session.AuthnStatementType;


/**
 * <h2>{@link SessionAssertion}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Apr 3, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class SessionAssertion {

    private final String      subject;

    private final String      applicationPool;

    private Map<Date, String> authentications;


    public SessionAssertion(AssertionType assertion) {

        subject = assertion.getSubject();
        applicationPool = assertion.getApplicationPool();
        authentications = new HashMap<Date, String>();
        for (AuthnStatementType statement : assertion.getAuthnStatement()) {
            authentications.put(statement.getTime().toGregorianCalendar().getTime(), statement.getDevice());
        }
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
