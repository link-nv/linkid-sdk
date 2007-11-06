package net.link.safeonline.authentication.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class MobileRegistrationException extends SafeOnlineException {

	private static final long serialVersionUID = 1L;

}
