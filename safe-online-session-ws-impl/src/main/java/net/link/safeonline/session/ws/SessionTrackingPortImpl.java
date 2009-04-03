/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.session.ws;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.lin_k.safe_online.session.ApplicationPoolType;
import net.lin_k.safe_online.session.AssertionType;
import net.lin_k.safe_online.session.AuthnStatementType;
import net.lin_k.safe_online.session.SessionTrackingPort;
import net.lin_k.safe_online.session.SessionTrackingRequestType;
import net.lin_k.safe_online.session.SessionTrackingResponseType;
import net.lin_k.safe_online.session.StatusType;
import net.link.safeonline.authentication.exception.ApplicationPoolNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ApplicationIdentifierMappingService;
import net.link.safeonline.authentication.service.SessionTrackingService;
import net.link.safeonline.entity.sessiontracking.SessionAssertionEntity;
import net.link.safeonline.entity.sessiontracking.SessionAuthnStatementEntity;
import net.link.safeonline.ws.common.SessionTrackingErrorCode;
import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of OLAS Session Tracking web service using JAX-WS.
 * 
 * @author wvdhaute
 * 
 */
@WebService(endpointInterface = "net.lin_k.safe_online.session.SessionTrackingPort")
@HandlerChain(file = "auth-ws-handlers.xml")
@Injection
public class SessionTrackingPortImpl implements SessionTrackingPort {

    private static final Log            LOG = LogFactory.getLog(SessionTrackingPortImpl.class);

    private DatatypeFactory             datatypeFactory;

    @EJB(mappedName = SessionTrackingService.JNDI_BINDING)
    SessionTrackingService              sessionTrackingService;

    @EJB(mappedName = ApplicationIdentifierMappingService.JNDI_BINDING)
    ApplicationIdentifierMappingService applicationIdentifierMappingService;


    @PostConstruct
    public void postConstructCallback() {

        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new EJBException("datatype config error");
        }

        LOG.debug("ready");
    }

    /**
     * {@inheritDoc}
     */
    public SessionTrackingResponseType getAssertions(SessionTrackingRequestType request) {

        LOG.debug("getAssertions");

        List<String> applicationPools = new LinkedList<String>();
        for (ApplicationPoolType applicationPool : request.getApplicationPools()) {
            applicationPools.add(applicationPool.getName());
        }

        LOG.debug("get assertions for session=" + request.getSession() + " subject=" + request.getSubject());
        try {
            List<SessionAssertionEntity> assertions = sessionTrackingService.getAssertions(request.getSession(), request.getSubject(),
                    applicationPools);
            SessionTrackingResponseType response = createGenericResponse(SessionTrackingErrorCode.SUCCESS);
            for (SessionAssertionEntity assertion : assertions) {
                response.getAssertions().add(getAssertion(assertion));
            }
            return response;
        } catch (SubjectNotFoundException e) {
            LOG.debug("subject not found: " + e.getMessage());
            return createGenericResponse(SessionTrackingErrorCode.SUBJECT_NOT_FOUND);
        } catch (ApplicationPoolNotFoundException e) {
            LOG.debug("application pool not found: " + e.getMessage());
            return createGenericResponse(SessionTrackingErrorCode.APPLICATION_POOL_NOT_FOUND);
        }
    }

    private AssertionType getAssertion(SessionAssertionEntity assertion) {

        AssertionType assertionType = new AssertionType();
        assertionType.setApplicationPool(assertion.getApplicationPool().getName());

        /*
         * Map OLAS user ID to application user ID
         */
        String applicationUserId = applicationIdentifierMappingService.getApplicationUserId(assertion.getSubject());
        assertionType.setSubject(applicationUserId);

        for (SessionAuthnStatementEntity statement : assertion.getStatements()) {
            AuthnStatementType authnStatement = new AuthnStatementType();
            authnStatement.setDevice(statement.getDevice().getName());
            authnStatement.setTime(getXmlGregorianCalendar(statement.getAuthenticationTime()));
            assertionType.getAuthnStatement().add(authnStatement);
        }

        return assertionType;
    }

    private SessionTrackingResponseType createGenericResponse(SessionTrackingErrorCode errorCode) {

        SessionTrackingResponseType response = new SessionTrackingResponseType();

        StatusType status = new StatusType();
        status.setValue(errorCode.getErrorCode());
        response.setStatus(status);
        return response;
    }

    private XMLGregorianCalendar getXmlGregorianCalendar(Date date) {

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(date);
        XMLGregorianCalendar xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        return xmlGregorianCalendar;
    }

}
