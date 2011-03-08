/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws.session;

import java.util.List;
import net.link.safeonline.sdk.logging.exception.ApplicationPoolNotFoundException;
import net.link.safeonline.sdk.logging.exception.RequestDeniedException;
import net.link.safeonline.sdk.logging.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.WSClient;


/**
 * <h2>{@link SessionTrackingClient}</h2>
 * 
 * <p>
 * Interface for the session tracking client. Via implementations of this service applications can retrieve session tracking information.
 * That is it will return a list of session tracking assertions.
 * </p>
 * 
 * <p>
 * <i>Apr 3, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public interface SessionTrackingClient extends WSClient {

    /**
     * Returns list of session tracking assertions, given a required session field and optional subject / application pools.
     * 
     * @param subject optional subject field
     * @param applicationPools optional list of application pool names
     * 
     * @return list of {@link SessionAssertion}. List is empty if no session tracking assertions were found for the specified criteria.
     * 
     * @throws SubjectNotFoundException
     * @throws ApplicationPoolNotFoundException
     * @throws RequestDeniedException
     */
    public List<SessionAssertion> getAssertions(String session, String subject, List<String> applicationPools)
            throws WSClientTransportException, ApplicationPoolNotFoundException, SubjectNotFoundException, RequestDeniedException;
}
