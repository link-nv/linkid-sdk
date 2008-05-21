package net.link.safeonline.digipass;

import javax.ejb.Local;

@Local
public interface Authentication {

	/*
	 * Accessors.
	 */
	String getLoginName();

	void setLoginName(String loginName);

	String getToken();

	void setToken(String token);

	/*
	 * Actions.
	 */
	String login();

	String cancel();

	/*
	 * Lifecycle.
	 */
	void init();

	void destroyCallback();

}
