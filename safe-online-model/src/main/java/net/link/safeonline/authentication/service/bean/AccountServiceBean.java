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

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.AccountService;
import net.link.safeonline.authentication.service.AccountServiceRemote;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class AccountServiceBean implements AccountService, AccountServiceRemote {

	private static final Log LOG = LogFactory.getLog(AccountServiceBean.class);

	@EJB
	private SubjectManager subjectManager;

	@EJB
	private HistoryDAO historyDAO;
	
	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private SubjectDAO subjectDAO;
	
	@EJB
	private SubjectIdentifierDAO subjectIdentifierDAO;
	
	
	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void removeAccount() {
		LOG.debug("remove account");
		SubjectEntity subject = this.subjectManager.getCallerSubject();

		this.historyDAO.clearAllHistory(subject);
		this.subscriptionDAO.removeAllSubscriptions(subject);
		this.attributeDAO.removeAttributes(subject);
		this.subjectIdentifierDAO.removeSubjectIdentifiers(subject);
		this.subjectDAO.removeSubject(subject);
	}
}
