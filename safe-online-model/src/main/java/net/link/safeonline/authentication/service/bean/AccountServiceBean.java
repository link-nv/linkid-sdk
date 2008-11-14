/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.AccountService;
import net.link.safeonline.authentication.service.AccountServiceRemote;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationScopeIdDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.NodeMappingDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.notification.service.NotificationProducerService;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = AccountService.JNDI_BINDING)
@RemoteBinding(jndiBinding = AccountServiceRemote.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class AccountServiceBean implements AccountService, AccountServiceRemote {

    private static final Log            LOG = LogFactory.getLog(AccountServiceBean.class);

    @EJB(mappedName = SubjectManager.JNDI_BINDING)
    private SubjectManager              subjectManager;

    @EJB(mappedName = HistoryDAO.JNDI_BINDING)
    private HistoryDAO                  historyDAO;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO                attributeDAO;

    @EJB(mappedName = SubscriptionDAO.JNDI_BINDING)
    private SubscriptionDAO             subscriptionDAO;

    @EJB(mappedName = ApplicationScopeIdDAO.JNDI_BINDING)
    private ApplicationScopeIdDAO       applicationScopeIdDAO;

    @EJB(mappedName = SubjectDAO.JNDI_BINDING)
    private SubjectDAO                  subjectDAO;

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO        subjectIdentifierDAO;

    @EJB(mappedName = NodeMappingDAO.JNDI_BINDING)
    private NodeMappingDAO              nodeMappingDAO;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService              subjectService;

    @EJB(mappedName = NotificationProducerService.JNDI_BINDING)
    private NotificationProducerService notificationProducerService;


    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public void removeAccount()
            throws SubscriptionNotFoundException, MessageHandlerNotFoundException {

        SubjectEntity subject = this.subjectManager.getCallerSubject();
        removeSubject(subject);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeAccount(SubjectEntity subject)
            throws SubscriptionNotFoundException, MessageHandlerNotFoundException {

        removeSubject(subject);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public void removeAccount(String userId)
            throws SubjectNotFoundException, SubscriptionNotFoundException, MessageHandlerNotFoundException {

        SubjectEntity subject = this.subjectService.getSubject(userId);
        removeSubject(subject);
    }

    private void removeSubject(SubjectEntity subject)
            throws MessageHandlerNotFoundException {

        LOG.debug("remove account: " + subject.getUserId());

        this.notificationProducerService.sendNotification(SafeOnlineConstants.TOPIC_REMOVE_USER, subject.getUserId(), null);

        this.historyDAO.clearAllHistory(subject);
        this.subscriptionDAO.removeAllSubscriptions(subject);
        this.applicationScopeIdDAO.removeApplicationScopeIds(subject);
        this.attributeDAO.removeAttributes(subject);
        this.subjectIdentifierDAO.removeSubjectIdentifiers(subject);
        this.nodeMappingDAO.removeNodeMappings(subject);
        this.subjectDAO.removeSubject(subject);
    }
}
