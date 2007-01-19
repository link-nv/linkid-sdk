/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.bean;

import javax.ejb.Stateful;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.ctrl.bean.LoginBaseBean;
import net.link.safeonline.oper.Login;
import net.link.safeonline.oper.OperatorConstants;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * This Seam components implements the login interface. This component cannot
 * live within the security domain of the SafeOnline user web application since
 * the user still has to logon onto the system.
 * 
 * @author fcorneli
 * 
 * Seam component name lookup clashes between two WARs. Because of this we have
 * to prefix the user and operator Seam components' names.
 * 
 * Because of http session timeout being set to 5 minutes in web.xml we have to
 * make sure that the lifecycle of the login bean that has session scope is
 * longer than 5 minutes. Thus we take 5 + 1 minutes.
 * 
 */
@Stateful
@Name("operLogin")
@Scope(ScopeType.SESSION)
@CacheConfig(idleTimeoutSeconds = (5 + 1) * 60)
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX + "LoginBean/local")
public class LoginBean extends LoginBaseBean implements Login {

	public LoginBean() {
		super(SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME);
	}
}
