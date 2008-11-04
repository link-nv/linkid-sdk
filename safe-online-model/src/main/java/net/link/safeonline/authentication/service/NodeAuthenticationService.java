/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.entity.NodeEntity;


/**
 * Interface for node authentication service.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface NodeAuthenticationService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/NodeAuthenticationServiceBean/local";


    /**
     * Authenticates an olas node given a node's certificate. At this point the node's certificate already passed the PKI validation.
     * 
     * @param authnCertificate
     *            the trusted X509 node certificate.
     * @return the node name.
     * @throws NodeNotFoundException
     */
    String authenticate(X509Certificate authnCertificate)
            throws NodeNotFoundException;

    /**
     * Gives back the node's signing certificates given the node name.
     * 
     * @param nodeName
     *            the node name.
     * @return the signing X509 node certificates.
     * @throws NodeNotFoundException
     */
    List<X509Certificate> getSigningCertificates(String nodeName)
            throws NodeNotFoundException;

    /**
     * Gives back the node entity given the node name.
     * 
     * @param nodeName
     * @return the OLAS node entity
     * @throws NodeNotFoundException
     */
    NodeEntity getNode(String nodeName)
            throws NodeNotFoundException;

    /**
     * Gives back the node entity at this location.
     * 
     * @return
     * @throws NodeNotFoundException
     */
    NodeEntity getLocalNode()
            throws NodeNotFoundException;
}
