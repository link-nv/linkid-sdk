package net.link.safeonline.authentication.service;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class SubscriptionNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
}
