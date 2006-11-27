package net.link.safeonline.authentication.service;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class EntityNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
}
