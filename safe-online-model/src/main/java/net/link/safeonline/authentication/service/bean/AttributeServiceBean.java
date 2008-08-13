/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineApplicationRoles;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AttributeService;
import net.link.safeonline.authentication.service.AttributeServiceRemote;
import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.model.ApplicationManager;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;


/**
 * Attribute Service Implementation for applications.
 *
 * @author fcorneli
 *
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_APPLICATION_SECURITY_DOMAIN)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class AttributeServiceBean implements AttributeService, AttributeServiceRemote {

    private static final Log       LOG = LogFactory.getLog(AttributeServiceBean.class);

    @EJB
    private ApplicationManager     applicationManager;

    @EJB
    private ApplicationIdentityDAO applicationIdentityDAO;

    @EJB
    private SubscriptionDAO        subscriptionDAO;

    @EJB
    private SubjectService         subjectService;

    @EJB
    private ProxyAttributeService  proxyAttributeService;


    @RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
    public Object getConfirmedAttributeValue(String subjectLogin, String attributeName)
            throws AttributeNotFoundException, PermissionDeniedException, SubjectNotFoundException,
            AttributeTypeNotFoundException {

        LOG.debug("get attribute " + attributeName + " for login " + subjectLogin);
        List<ApplicationIdentityAttributeEntity> confirmedAttributes = getConfirmedIdentityAttributes(subjectLogin);

        AttributeTypeEntity attributeType = checkAttributeReadPermission(attributeName, confirmedAttributes);
        SubjectEntity subject = this.subjectService.getSubject(subjectLogin);
        return this.proxyAttributeService.findAttributeValue(subject.getUserId(), attributeType.getName());
    }

    private AttributeTypeEntity checkAttributeReadPermission(String attributeName,
            List<ApplicationIdentityAttributeEntity> attributes) throws PermissionDeniedException {

        for (ApplicationIdentityAttributeEntity attribute : attributes) {
            LOG.debug("identity attribute: " + attribute.getAttributeTypeName());
            if (attribute.getAttributeTypeName().equals(attributeName))
                return attribute.getAttributeType();
        }
        LOG.debug("attribute not in set of confirmed identity attributes");
        throw new PermissionDeniedException("attribute not in set of confirmed identity attributes");
    }

    private List<ApplicationIdentityAttributeEntity> getConfirmedIdentityAttributes(String subjectLogin)
            throws SubjectNotFoundException, PermissionDeniedException {

        SubjectEntity subject = this.subjectService.getSubject(subjectLogin);
        ApplicationEntity application = this.applicationManager.getCallerApplication();

        /*
         * The subject needs to be subscribed onto this application.
         */
        SubscriptionEntity subscription = this.subscriptionDAO.findSubscription(subject, application);
        if (null == subscription) {
            LOG.debug("subject is not subscribed");
            throw new PermissionDeniedException("subject is not subscribed");
        }

        /*
         * The subject needs to have a confirmed identity version.
         */
        Long confirmedIdentityVersion = subscription.getConfirmedIdentityVersion();
        if (null == confirmedIdentityVersion) {
            LOG.debug("subject has no confirmed identity version");
            throw new PermissionDeniedException("subject has no confirmed identity version");
        }

        ApplicationIdentityEntity confirmedApplicationIdentity;
        try {
            confirmedApplicationIdentity = this.applicationIdentityDAO.getApplicationIdentity(application,
                    confirmedIdentityVersion);
        } catch (ApplicationIdentityNotFoundException e) {
            throw new EJBException("application identity not found for version: " + confirmedIdentityVersion);
        }

        /*
         * Filter the data mining attributes
         */
        List<ApplicationIdentityAttributeEntity> attributes = new ArrayList<ApplicationIdentityAttributeEntity>();
        for (ApplicationIdentityAttributeEntity attribute : confirmedApplicationIdentity.getAttributes())
            if (!attribute.isDataMining()) {
                attributes.add(attribute);
            }
        return attributes;
    }

    @RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
    public Map<String, Object> getConfirmedAttributeValues(String subjectLogin) throws SubjectNotFoundException,
            PermissionDeniedException, AttributeTypeNotFoundException {

        LOG.debug("get confirmed attributes for subject: " + subjectLogin);
        List<ApplicationIdentityAttributeEntity> confirmedAttributes = getConfirmedIdentityAttributes(subjectLogin);
        Map<String, Object> resultAttributes = new TreeMap<String, Object>();
        SubjectEntity subject = this.subjectService.getSubject(subjectLogin);
        for (ApplicationIdentityAttributeEntity confirmedAttribute : confirmedAttributes) {
            String attributeName = confirmedAttribute.getAttributeTypeName();
            Object value = this.proxyAttributeService.findAttributeValue(subject.getUserId(), attributeName);
            if (null == value) {
                continue;
            }
            LOG.debug("confirmed attribute: " + attributeName);
            resultAttributes.put(attributeName, value);
        }
        return resultAttributes;
    }
}
