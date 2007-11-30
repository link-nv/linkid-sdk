/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import net.link.safeonline.auth.AccountRegistration;
import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.core.ResourceBundle;
import org.jboss.seam.log.Log;

import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;

@Stateful
@Name("accountRegistration")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "AccountRegistrationBean/local")
public class AccountRegistrationBean extends AbstractLoginBean implements
		AccountRegistration {

	@EJB
	private UserRegistrationService userRegistrationService;

	@EJB
	private DevicePolicyService devicePolicyService;

	@In
	private AuthenticationService authenticationService;

	@Logger
	private Log log;

	private String login;

	private String device;

	private String password;

	private String mobile;

	private String mobileOTP;

	private String captcha;

	@SuppressWarnings("unused")
	@In(value = AccountRegistration.REQUESTED_USERNAME_ATTRIBUTE, required = false, scope = ScopeType.SESSION)
	@Out(value = AccountRegistration.REQUESTED_USERNAME_ATTRIBUTE, required = false, scope = ScopeType.SESSION)
	private String requestedUsername;

	@In(required = false, scope = ScopeType.SESSION)
	@Out(required = false, scope = ScopeType.SESSION)
	private String challengeId;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.log.debug("destroy");
	}

	@Create
	@Begin
	public void begin() {
		this.log.debug("begin");
	}

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String loginNext() {
		this.log.debug("loginNext");

		this.log.debug("captcha: " + this.captcha);

		if (null == this.captchaService) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorNoCaptcha");
			return null;
		}

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		HttpSession httpSession = (HttpSession) externalContext
				.getSession(false);
		String captchaId = httpSession.getId();
		this.log.debug("captcha Id: " + captchaId);

		boolean valid;
		try {
			valid = this.captchaService.validateResponseForID(captchaId,
					this.captcha);
		} catch (CaptchaServiceException e) {
			/*
			 * It's possible that a data race occurs between the Captcha servlet
			 * and this validation call. In that case we just ask the user to
			 * try again.
			 */
			this.facesMessages.addToControlFromResourceBundle("captcha",
					FacesMessage.SEVERITY_ERROR, "errorNoCaptchaValidation");
			return null;
		}
		if (false == valid) {
			this.facesMessages.addToControlFromResourceBundle("captcha",
					FacesMessage.SEVERITY_ERROR, "errorInvalidCaptcha");
			return null;
		}

		boolean loginFree = this.userRegistrationService
				.isLoginFree(this.login);
		if (false == loginFree) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorLoginTaken");
			return null;
		}

		/*
		 * The requestedUsername session attribute can be used during the device
		 * specific registration process. For example, the registration
		 * statement is using it.
		 */
		this.requestedUsername = this.login;

		return "next";
	}

	public String deviceNext() {
		this.log.debug("deviceNext");
		if (null == this.device) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceSelection");
			return null;
		}
		this.log.debug("device: " + this.device);
		return this.device;
	}

	public String getDevice() {
		return this.device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getPassword() {
		return this.password;
	}

	@In(required = false, value = "CaptchaService", scope = ScopeType.SESSION)
	ImageCaptchaService captchaService;

	public String passwordNext() {
		this.log.debug("passwordNext");

		super.clearUsername();

		try {
			this.userRegistrationService
					.registerUser(this.login, this.password);
		} catch (ExistingUserException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorLoginTaken");
			return null;
		} catch (AttributeTypeNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorAttributeTypeNotFound");
			return null;
		}

		try {
			boolean authenticated = this.authenticationService.authenticate(
					this.login, this.password);
			if (false == authenticated) {
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
				return null;
			}
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addToControlFromResourceBundle("username",
					FacesMessage.SEVERITY_ERROR, "subjectNotFoundMsg");
			return null;
		} catch (DeviceNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPasswordNotFound");
			return null;
		}

		super.login(this.login, AuthenticationDevice.PASSWORD);
		return null;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCaptcha() {
		return this.captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}

	public String getMobile() {
		return this.mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String mobileNext() {
		this.log.debug("mobileNext");
		super.clearUsername();

		try {
			this.authenticationService.authenticate(
					AuthenticationDevice.WEAK_MOBILE, this.mobile,
					this.challengeId, this.mobileOTP);
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
			HelpdeskLogger.add("login: subject not found for " + this.login,
					LogLevelType.ERROR);
			return null;
		} catch (MalformedURLException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
			HelpdeskLogger.add("login: encap webservice not available",
					LogLevelType.ERROR);
			return null;
		} catch (MobileException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
			HelpdeskLogger.add("login: failed to contact encap webservice for "
					+ this.login, LogLevelType.ERROR);
			return null;
		} catch (MobileAuthenticationException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileAuthenticationFailed");
			return null;
		}

		super.login(this.login, AuthenticationDevice.WEAK_MOBILE);
		this.challengeId = null;
		return null;
	}

	public String mobileRegister() {
		this.log.debug("mobile register: " + this.mobile);
		try {
			this.userRegistrationService.registerMobile(this.requestedUsername,
					this.mobile);
		} catch (ExistingUserException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorLoginTaken");
			return null;
		} catch (MobileException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
			return null;
		} catch (MalformedURLException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
			return null;
		} catch (MobileRegistrationException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
			return null;
		} catch (AttributeTypeNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorAttributeTypeNotFound");
			return null;
		} catch (ArgumentIntegrityException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorMobileTaken");
			return null;
		}
		try {
			this.challengeId = this.userRegistrationService
					.requestMobileOTP(this.mobile);
			this.log.debug("recevied challengeId: " + this.challengeId);
		} catch (MalformedURLException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
			return null;
		} catch (MobileException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
			return null;
		}
		return null;
	}

	@Factory("allDevices")
	public List<SelectItem> allDevicesFactory() {
		this.log.debug("all devices factory");
		List<SelectItem> allDevices = new LinkedList<SelectItem>();
		Set<AuthenticationDevice> devices = this.devicePolicyService
				.getDevices();
		for (AuthenticationDevice authDevice : devices) {
			String deviceName = authDevice.getDeviceName();
			SelectItem allDevice = new SelectItem(deviceName);
			allDevices.add(allDevice);
		}
		deviceNameDecoration(allDevices);
		return allDevices;
	}

	private void deviceNameDecoration(List<SelectItem> selectItems) {
		for (SelectItem selectItem : selectItems) {
			String deviceId = (String) selectItem.getValue();
			try {
				java.util.ResourceBundle bundle = ResourceBundle.instance();
				String deviceName = bundle.getString(deviceId);
				if (null == deviceName) {
					deviceName = deviceId;
				}
				selectItem.setLabel(deviceName);

			} catch (MissingResourceException e) {
				this.log.debug("resource not found: " + deviceId);
			}
		}
	}

	public String getMobileOTP() {
		return this.mobileOTP;
	}

	public void setMobileOTP(String mobileOTP) {
		this.mobileOTP = mobileOTP;
	}

}
