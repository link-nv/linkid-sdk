package net.link.safeonline.beid;

import javax.ejb.Local;

@Local
public interface Main {
	/*
	 * Accessor
	 */
	String getRedirectUrl();

	/*
	 * Lifecycle callbacks
	 */
	void init();

	void destroyCallback();

}
