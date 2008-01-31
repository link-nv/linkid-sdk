package net.link.safeonline.encap;

import javax.ejb.Local;

@Local
public interface MobileLogon {

	/*
	 * Accessors.
	 */
	String getMobileOTP();

	void setMobileOTP(String mobileOTP);

	String getMobile();

	void setMobile(String mobile);

	String getChallengeId();

	void setChallengeId(String challengeId);

	/*
	 * Actions.
	 */
	String login();

	String requestOTP();

	/*
	 * Lifecycle.
	 */
	void init();

	void destroyCallback();

}
