/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.data.AccountMergingDO;

@Local
public interface AccountMergingService {

	/**
	 * Figure out what will be removed, kept or imported from the source
	 * account.
	 * 
	 * @param sourceAccountName
	 * @return
	 * @throws SubjectNotFoundException
	 * @throws AttributeTypeNotFoundException
	 * @throws EmptyDevicePolicyException
	 * @throws ApplicationNotFoundException
	 */
	AccountMergingDO getAccountMergingDO(String sourceAccountName)
			throws SubjectNotFoundException, AttributeTypeNotFoundException,
			ApplicationNotFoundException, EmptyDevicePolicyException;

	/**
	 * Commit the calculated changes from merging with the source account.
	 * 
	 * @param accountMergingDO
	 * @param neededDevices
	 * @throws AttributeTypeNotFoundException
	 * @throws SubjectNotFoundException
	 * @throws PermissionDeniedException
	 */
	void mergeAccount(AccountMergingDO accountMergingDO,
			Set<AuthenticationDevice> neededDevices)
			throws AttributeTypeNotFoundException, SubjectNotFoundException,
			PermissionDeniedException;

}
