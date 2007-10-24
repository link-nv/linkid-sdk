package net.link.safeonline.auth;

import javax.ejb.Local;

@Local
public interface GlobalUsageAgreementConfirmation {

	/*
	 * Actions.
	 */
	String confirm();

	/*
	 * Accessors
	 */
	String getUsageAgreement();

}
