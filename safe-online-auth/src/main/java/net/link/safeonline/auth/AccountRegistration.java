/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import javax.ejb.Local;

@Local
public interface AccountRegistration {

	static final String REQUESTED_USERNAME_ATTRIBUTE = "requestedUsername";

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	void begin();

	/*
	 * Accessors.
	 */
	String getLogin();

	void setLogin(String login);

	String getDevice();

	void setDevice(String device);

	String getPassword();

	void setPassword(String password);

	String getCaptcha();

	void setCaptcha(String captcha);

	/*
	 * Actions.
	 */
	String loginNext();

	String deviceNext();

	String passwordNext();
}
