/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.security.cert.X509Certificate;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.NodeNotFoundException;

/**
 * Interface for node authentication service.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface NodeAuthenticationService {

	/**
	 * Authenticates an olas node given a node's certificate. At this point the
	 * node's certificate already passed the PKI validation.
	 * 
	 * @param certificate
	 *            the trusted X509 node certificate.
	 * @return the node name.
	 * @throws NodeNotFoundException
	 */
	String authenticate(X509Certificate certificate)
			throws NodeNotFoundException;

	/**
	 * Gives back the node's X509 certificate given the node name.
	 * 
	 * @param nodeName
	 *            the node name.
	 * @return the X509 node certificate.
	 * @throws NodeNotFoundException
	 */
	X509Certificate getCertificate(String nodeName)
			throws NodeNotFoundException;
}
