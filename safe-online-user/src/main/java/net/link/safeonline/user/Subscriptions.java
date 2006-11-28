package net.link.safeonline.user;

import javax.ejb.Local;

@Local
public interface Subscriptions {

	void subscriptionListFactory();

	String view();

	String unsubscribe();
}
