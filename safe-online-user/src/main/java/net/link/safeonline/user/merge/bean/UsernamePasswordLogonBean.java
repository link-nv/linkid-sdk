/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.merge.bean;

import javax.annotation.PostConstruct;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectMismatchException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ReAuthenticationService;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.user.UserConstants;
import net.link.safeonline.user.merge.UsernamePasswordLogon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("mergeUsernamePasswordLogon")
@LocalBinding(jndiBinding = UserConstants.JNDI_PREFIX
		+ "UsernamePasswordLogonBean/local")
public class UsernamePasswordLogonBean implements UsernamePasswordLogon {

	private static final Log LOG = LogFactory
			.getLog(UsernamePasswordLogonBean.class);

	@In(required = true)
	private String source;

	private String password;

	@In
	private ReAuthenticationService reAuthenticationService;

	@In(create = true)
	FacesMessages facesMessages;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.password = null;
	}

	public String getPassword() {
		return this.password;
	}

	public String getSource() {
		return this.source;
	}

	@PostConstruct
	public void init() {
		HelpdeskLogger.clear();
	}

	public String login() {
		LOG.debug("login: " + this.source);
		HelpdeskLogger.add("login: " + this.source, LogLevelType.INFO);

		try {
			boolean authenticated = this.reAuthenticationService.authenticate(
					this.source, this.password);
			if (false == authenticated) {
				/*
				 * The abort will be correctly handled by the authentication
				 * service manager. That way we allow the user to retry the
				 * initial authentication step.
				 */
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
				HelpdeskLogger.add("login failed: " + this.source,
						LogLevelType.ERROR);
				return null;
			}
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
			HelpdeskLogger.add("login: subject not found for " + this.source,
					LogLevelType.ERROR);
			return null;
		} catch (DeviceNotFoundException e) {
			/*
			 * Important here not to explicitly communicate that the password
			 * device was not configured.
			 */
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
			HelpdeskLogger.add("password device not configured",
					LogLevelType.ERROR);
			return null;
		} catch (SubjectMismatchException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "subjectMismatchMsg");
			HelpdeskLogger
					.add(
							"subject does not match already authenticated source subject",
							LogLevelType.ERROR);
			return null;
		} catch (PermissionDeniedException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			HelpdeskLogger.add("source subject equals target subject",
					LogLevelType.ERROR);
			return null;
		}
		LOG.debug("authenticated " + this.source);
		HelpdeskLogger.clear();
		return "success";
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
