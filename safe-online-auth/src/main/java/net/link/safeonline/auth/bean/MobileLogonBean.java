package net.link.safeonline.auth.bean;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.annotation.PostConstruct;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.MobileLogon;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Stateful
@Name("mobileLogon")
@Scope(ScopeType.SESSION)
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "MobileLogonBean/local")
public class MobileLogonBean extends AbstractLoginBean implements MobileLogon {

	private static final Log LOG = LogFactory.getLog(MobileLogonBean.class);

	@In
	private AuthenticationService authenticationService;

	@In(required = true)
	private AuthenticationDevice deviceSelection;

	private String challengeId;

	private String loginname;

	private String mobile;

	private String mobileOTP;

	public String getMobileOTP() {
		return this.mobileOTP;
	}

	public void setMobileOTP(String mobileOTP) {
		this.mobileOTP = mobileOTP;
	}

	public String getMobile() {
		return this.mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getChallengeId() {
		return this.challengeId;
	}

	public void setChallengeId(String challengeId) {
		this.challengeId = challengeId;
	}

	public String getLoginname() {
		return this.loginname;
	}

	public void setLoginname(String loginname) {
		this.loginname = loginname;
	}

	public String login() {
		LOG.debug("login: " + this.loginname);
		HelpdeskLogger.add("login: " + this.loginname, LogLevelType.INFO);
		super.clearUsername();

		try {
			boolean authenticated = this.authenticationService.authenticate(
					this.deviceSelection, this.loginname, this.challengeId,
					this.mobileOTP);
			if (false == authenticated) {
				/*
				 * The abort will be correctly handled by the authentication
				 * service manager. That way we allow the user to retry the
				 * initial authentication step.
				 */
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
				HelpdeskLogger.add("login failed: " + this.loginname,
						LogLevelType.ERROR);
				return null;
			}
		} catch (AxisFault e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
			HelpdeskLogger.add("login: failed to contact encap webservice for "
					+ this.loginname, LogLevelType.ERROR);
			return null;
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
			HelpdeskLogger.add(
					"login: subject not found for " + this.loginname,
					LogLevelType.ERROR);
			return null;
		} catch (MalformedURLException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
			HelpdeskLogger.add("login: encap webservice not available",
					LogLevelType.ERROR);
			return null;
		} catch (RemoteException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
			HelpdeskLogger.add("login: failed to contact encap webservice for "
					+ this.loginname, LogLevelType.ERROR);
			return null;
		} catch (MobileRegistrationException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
			return null;
		}

		super.login(this.loginname, this.deviceSelection);
		HelpdeskLogger.clear();
		destroyCallback();
		return null;
	}

	public String requestOTP() {
		LOG.debug("request OTP: user=" + this.loginname + " mobile="
				+ this.mobile);
		try {
			this.challengeId = this.authenticationService.requestMobileOTP(
					this.deviceSelection, this.mobile);
			LOG.debug("received challengeId: " + this.challengeId);
		} catch (MalformedURLException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
			return null;
		} catch (RemoteException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
			return null;
		}
		return null;
	}

	@PostConstruct
	public void init() {
		HelpdeskLogger.clear();
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("remove");
		this.mobileOTP = null;
	}

}
