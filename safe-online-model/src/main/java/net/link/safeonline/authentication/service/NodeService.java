/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ExistingNodeException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.OlasEntity;
import net.link.safeonline.pkix.exception.CertificateEncodingException;

/**
 * Interface to service for retrieving information about applications.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface NodeService {

	/**
	 * Gives back all known olas nodes
	 * 
	 */
	List<OlasEntity> listNodes();

	/**
	 * Gives back the olas node entity for a given node name.
	 * 
	 * @param nodeName
	 * @throws NodeNotFoundException
	 */
	OlasEntity getNode(String nodeName) throws NodeNotFoundException;

	/**
	 * @param name
	 * @param location
	 * @param encodedCertificate
	 * @throws ExistingNodeException
	 * @throws CertificateEncodingException
	 */
	void addNode(String name, String location, byte[] encodedCertificate)
			throws ExistingNodeException, CertificateEncodingException;

	/**
	 * Removes an olas node.
	 * 
	 * @param name
	 */
	void removeNode(String name) throws NodeNotFoundException,
			PermissionDeniedException;

	/**
	 * Updates the Olas node's location.
	 * 
	 * @param nodeName
	 * @param location
	 * @throws NodeNotFoundException
	 */
	void updateLocation(String nodeName, String location)
			throws NodeNotFoundException;

	/**
	 * Updates the OLAS node's certificate.
	 * 
	 * @param nodeName
	 * @param certificateData
	 * @throws CertificateEncodingException
	 * @throws NodeNotFoundException
	 */
	void updateCertificate(String nodeName, byte[] certificateData)
			throws CertificateEncodingException, NodeNotFoundException;
}
