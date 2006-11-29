package net.link.safeonline.user;

import javax.ejb.Local;

@Local
public interface Subscriptions {

	void subscriptionListFactory();

	String viewSubscription();

	String viewApplication();

	String unsubscribe();

	String subscribe();

	void applicationListFactory();

	void destroyCallback();
}
