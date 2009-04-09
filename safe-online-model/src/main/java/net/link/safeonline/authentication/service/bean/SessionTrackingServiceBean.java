/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.authentication.service.bean;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineApplicationRoles;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.ApplicationPoolNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ApplicationIdentifierMappingService;
import net.link.safeonline.authentication.service.SessionTrackingService;
import net.link.safeonline.dao.ApplicationPoolDAO;
import net.link.safeonline.dao.SessionTrackingDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.sessiontracking.SessionAssertionEntity;
import net.link.safeonline.entity.sessiontracking.SessionTrackingEntity;
import net.link.safeonline.model.ApplicationManager;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;


/**
 * <h2>{@link SessionTrackingServiceBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Apr 2, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_APPLICATION_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = SessionTrackingService.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class SessionTrackingServiceBean implements SessionTrackingService {

    private static final Log            LOG = LogFactory.getLog(SessionTrackingServiceBean.class);

    @EJB(mappedName = ApplicationManager.JNDI_BINDING)
    ApplicationManager                  applicationManager;

    @EJB(mappedName = ApplicationIdentifierMappingService.JNDI_BINDING)
    ApplicationIdentifierMappingService applicationIdentifierMappingService;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    SubjectService                      subjectService;

    @EJB(mappedName = ApplicationPoolDAO.JNDI_BINDING)
    ApplicationPoolDAO                  applicationPoolDAO;

    @EJB(mappedName = SessionTrackingDAO.JNDI_BINDING)
    SessionTrackingDAO                  sessionTrackingDAO;


    /**
     * {@inheritDoc}
     */
    @RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
    public List<SessionAssertionEntity> getAssertions(String session, String applicationUserId, List<String> applicationPoolNames)
            throws SubjectNotFoundException, ApplicationPoolNotFoundException {

        LOG.debug("get assertions");

        ApplicationEntity application = applicationManager.getCallerApplication();

        SubjectEntity subject = null;
        if (null != applicationUserId) {
            String userId = applicationIdentifierMappingService.findUserId(application, applicationUserId);
            subject = subjectService.getSubject(userId);
        }

        List<ApplicationPoolEntity> applicationPools = new LinkedList<ApplicationPoolEntity>();
        for (String applicationPoolName : applicationPoolNames) {
            applicationPools.add(applicationPoolDAO.getApplicationPool(applicationPoolName));
        }

        /*
         * Fetch session trackers
         */
        List<SessionTrackingEntity> trackers = new LinkedList<SessionTrackingEntity>();
        if (!applicationPools.isEmpty()) {
            for (ApplicationPoolEntity applicationPool : applicationPools) {
                trackers.addAll(sessionTrackingDAO.listTrackers(application, session, applicationPool));
            }
        } else {
            trackers.addAll(sessionTrackingDAO.listTrackers(application, session));
        }

        /*
         * From list of trackers, fetch session assertions
         */
        List<SessionAssertionEntity> assertions = new LinkedList<SessionAssertionEntity>();
        for (SessionTrackingEntity tracker : trackers) {

            SessionAssertionEntity assertion = sessionTrackingDAO.findAssertion(tracker);
            if (null != subject) {
                if (!assertion.getSubject().equals(subject)) {
                    continue;
                }
            }
            LOG.debug("tracker old timestamp: " + tracker.getTimestamp().toString());
            // Update timestamp for each tracker
            tracker.setTimestamp(new Date());
            LOG.debug("tracker new timestamp: " + tracker.getTimestamp().toString());

            assertion.setStatements(sessionTrackingDAO.listStatements(assertion));
            assertions.add(assertion);
        }

        return assertions;
    }
}
