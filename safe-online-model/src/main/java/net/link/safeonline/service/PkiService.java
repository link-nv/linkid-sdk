/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.authentication.exception.CertificateEncodingException;
import net.link.safeonline.authentication.exception.ExistingTrustDomainException;
import net.link.safeonline.authentication.exception.ExistingTrustPointException;
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.authentication.exception.TrustPointNotFoundException;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.entity.TrustPointEntity;

/**
 * Interface definition for PKI service component.
 * 
 * @author fcorneli
 * 
 */
@Local
@Remote
public interface PkiService {

	List<TrustDomainEntity> getTrustDomains();

	void addTrustDomain(String name) throws ExistingTrustDomainException;

	void removeTrustDomain(String name) throws TrustDomainNotFoundException;

	void addTrustPoint(String domainName, byte[] encodedCertificate)
			throws TrustDomainNotFoundException, CertificateEncodingException,
			ExistingTrustPointException;

	List<TrustPointEntity> getTrustPoints(String domainName)
			throws TrustDomainNotFoundException;

	void removeTrustPoint(TrustDomainEntity trustDomain, String subjectName)
			throws TrustPointNotFoundException;

	TrustDomainEntity getTrustDomain(String trustDomainName)
			throws TrustDomainNotFoundException;
}
