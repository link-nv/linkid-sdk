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
import net.link.safeonline.entity.OlasEntity;

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
	 * @param authnCertificate
	 *            the trusted X509 node certificate.
	 * @return the node name.
	 * @throws NodeNotFoundException
	 */
	String authenticate(X509Certificate authnCertificate)
			throws NodeNotFoundException;

	/**
	 * Gives back the node's authentication certificate given the node name.
	 * 
	 * @param nodeName
	 *            the node name.
	 * @return the authentication X509 node certificate.
	 * @throws NodeNotFoundException
	 */
	X509Certificate getAuthnCertificate(String nodeName)
			throws NodeNotFoundException;

	/**
	 * Gives back the node's signing certificate given the node name.
	 * 
	 * @param nodeName
	 *            the node name.
	 * @return the signing X509 node certificate.
	 * @throws NodeNotFoundException
	 */
	X509Certificate getSigningCertificate(String nodeName)
			throws NodeNotFoundException;

	/**
	 * Gives back the node entity given the node name.
	 * 
	 * @param nodeName
	 * @return the OLAS node entity
	 * @throws NodeNotFoundException
	 */
	OlasEntity getNode(String nodeName) throws NodeNotFoundException;

	/**
	 * Gives back the node entity at this location.
	 * 
	 * @return
	 * @throws NodeNotFoundException
	 */
	OlasEntity getLocalNode() throws NodeNotFoundException;
}
