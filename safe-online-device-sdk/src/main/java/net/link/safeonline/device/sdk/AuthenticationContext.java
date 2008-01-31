package net.link.safeonline.device.sdk;

import java.util.Set;

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

	private AuthenticationContext(HttpSession session) {
		this.session = session;
		this.session.setAttribute(LOGIN_MANAGER, this);
	}

	public static AuthenticationContext getLoginManager(HttpSession session) {
		AuthenticationContext instance = (AuthenticationContext) session
				.getAttribute(LOGIN_MANAGER);
		if (null == instance)
			instance = new AuthenticationContext(session);
		return instance;
	}

	public Set<String> getWantedDevices() {
		return this.wantedDevices;
	}

	public void setWantedDevices(Set<String> wantedDevices) {
		this.wantedDevices = wantedDevices;
	}

	public String getUsedDevice() {
		return this.usedDevice;
	}

	public void setUsedDevice(String usedDevice) {
		this.usedDevice = usedDevice;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getIssuer() {
		return this.issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public int getValidity() {
		return this.validity;
	}

	public void setValidity(int validity) {
		this.validity = validity;
	}

}
