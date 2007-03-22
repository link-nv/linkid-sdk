package net.link.safeonline.authentication.exception;

import javax.ejb.ApplicationException;

import net.link.safeonline.shared.SharedConstants;

@ApplicationException(rollback = true)
public class StatisticNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	public StatisticNotFoundException() {
		super(SharedConstants.STATISTIC_NOT_FOUND_ERROR);
	}

}
