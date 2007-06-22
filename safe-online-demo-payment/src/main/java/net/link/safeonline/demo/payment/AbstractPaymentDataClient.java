package net.link.safeonline.demo.payment;

public interface AbstractPaymentDataClient {
	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	void postConstructCallback();

	void postActivateCallback();

	void prePassivateCallback();
}
