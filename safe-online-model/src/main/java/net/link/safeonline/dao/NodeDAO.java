/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.entity.NodeEntity;


@Local
public interface NodeDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "NodeDAOBean/local";


    NodeEntity addNode(String name, String protocol, String hostname, int port, int sslPort, X509Certificate authnCertificate,
                       X509Certificate signingCertificate);

    List<NodeEntity> listNodes();

    NodeEntity findNode(String name);

    NodeEntity getNode(String name)
            throws NodeNotFoundException;

    NodeEntity getNodeFromAuthnCertificate(X509Certificate authnCertificate)
            throws NodeNotFoundException;

    NodeEntity getNodeFromSigningCertificate(X509Certificate signingCertificate)
            throws NodeNotFoundException;

    NodeEntity findNodeFromAuthnCertificate(X509Certificate authnCertificate);

    NodeEntity findNodeFromSigningCertificate(X509Certificate signingCertificate);

    void removeNode(NodeEntity node);
}
