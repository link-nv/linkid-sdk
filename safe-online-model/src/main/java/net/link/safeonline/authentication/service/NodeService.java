/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ExistingNodeException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.pkix.exception.CertificateEncodingException;


/**
 * Interface to service for retrieving information about applications.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface NodeService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/NodeServiceBean/local";

    /**
     * Gives back all known olas nodes
     * 
     */
    List<NodeEntity> listNodes();

    /**
     * Gives back the olas node entity for a given node name.
     * 
     * @param nodeName
     * @throws NodeNotFoundException
     */
    NodeEntity getNode(String nodeName) throws NodeNotFoundException;

    /**
     * @param name
     * @param location
     * @param encodedAuthnCertificate
     * @param encodedSigningCertificate
     * @throws ExistingNodeException
     * @throws CertificateEncodingException
     */
    void addNode(String name, String protocol, String hostname, int port, int sslPort, byte[] encodedAuthnCertificate,
                 byte[] encodedSigningCertificate) throws ExistingNodeException, CertificateEncodingException;

    /**
     * Removes an olas node.
     * 
     * @param name
     */
    void removeNode(String name) throws NodeNotFoundException, PermissionDeniedException;

    /**
     * Updates the Olas node's hostname.
     * 
     * @param nodeName
     * @param hostname
     * @param port
     * @param sslPort
     * @throws NodeNotFoundException
     */
    void updateLocation(String nodeName, String protocol, String hostname, int port, int sslPort) throws NodeNotFoundException;

    /**
     * Updates the OLAS node's authn certificate.
     * 
     * @param nodeName
     * @param certificateData
     * @throws CertificateEncodingException
     * @throws NodeNotFoundException
     */
    void updateAuthnCertificate(String nodeName, byte[] certificateData) throws CertificateEncodingException, NodeNotFoundException;

    /**
     * Updates the OLAS node's signing certificate.
     * 
     * @param nodeName
     * @param certificateData
     * @throws CertificateEncodingException
     * @throws NodeNotFoundException
     */
    void updateSigningCertificate(String nodeName, byte[] certificateData) throws CertificateEncodingException, NodeNotFoundException;
}
