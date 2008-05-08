/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.audit.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.audit.AuditPrincipal;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;

@Stateful
@Name("audit_principal")
@Scope(ScopeType.CONVERSATION)
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "AuditPrincipalBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class AuditPrincipalBean implements AuditPrincipal {

	private static final Log LOG = LogFactory.getLog(AuditPrincipalBean.class);

	@In(create = true)
	FacesMessages facesMessages;

	@In(required = true)
	@Out(required = true)
	String principal;

	@EJB
	private SubjectService subjectService;

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String getName() {
		String name;
		if (!this.principal.equals(OperatorConstants.UNKNOWN_PRINCIPAL))
			name = this.subjectService.getSubjectLogin(this.principal);
		else
			name = OperatorConstants.UNKNOWN_PRINCIPAL;
		return name;
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy");
	}
}
