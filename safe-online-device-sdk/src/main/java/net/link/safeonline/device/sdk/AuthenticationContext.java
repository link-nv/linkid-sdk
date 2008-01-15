package net.link.safeonline.device.sdk;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AuthenticationContext {

	private HttpSession session;

	private Set<String> wantedDevices;

	private String usedDevice;

	private String userId;

	private String application;

	private String issuer;

	private int validity;

	public static final String LOGIN_MANAGER = AuthenticationContext.class
			.getName()
			+ ".LOGIN_MANAGER";

	private AuthenticationContext(HttpServletRequest request) {
		this.session = request.getSession();
		this.session.setAttribute(LOGIN_MANAGER, this);
	}

	public static AuthenticationContext getLoginManager(
			HttpServletRequest request) {
		AuthenticationContext instance = (AuthenticationContext) request
				.getSession().getAttribute(LOGIN_MANAGER);
		if (null == instance) {
			instance = new AuthenticationContext(request);
		}
		return instance;
	}

	public Set<String> getWantedDevices() {
		return wantedDevices;
	}

	public void setWantedDevices(Set<String> wantedDevices) {
		this.wantedDevices = wantedDevices;
	}

	public String getUsedDevice() {
		return usedDevice;
	}

	public void setUsedDevice(String usedDevice) {
		this.usedDevice = usedDevice;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public int getValidity() {
		return validity;
	}

	public void setValidity(int validity) {
		this.validity = validity;
	}

}
