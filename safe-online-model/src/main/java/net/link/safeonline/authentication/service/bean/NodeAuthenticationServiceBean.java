/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.dao.NodeDAO;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.pkix.dao.TrustPointDAO;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of node authentication service.
 * 
 * @author wvdhaute
 * 
 */
@Stateless
public class NodeAuthenticationServiceBean implements NodeAuthenticationService {

    private static final Log LOG = LogFactory.getLog(NodeAuthenticationServiceBean.class);

    @EJB
    private NodeDAO          olasDAO;

    @EJB
    private TrustPointDAO    trustPointDAO;


    public String authenticate(X509Certificate authnCertificate) throws NodeNotFoundException {

        NodeEntity node = this.olasDAO.getNodeFromAuthnCertificate(authnCertificate);
        String nodeName = node.getName();
        LOG.debug("authenticated node: " + nodeName);
        return nodeName;
    }

    public List<X509Certificate> getSigningCertificates(String nodeName) throws NodeNotFoundException {

        LOG.debug("get signing certificate for node: " + nodeName);
        NodeEntity node = this.olasDAO.getNode(nodeName);
        List<TrustPointEntity> trustPoints = this.trustPointDAO.listTrustPoints(node.getSigningCertificateSubject());
        List<X509Certificate> certificates = new LinkedList<X509Certificate>();
        for (TrustPointEntity trustPoint : trustPoints) {
            certificates.add(trustPoint.getCertificate());

        }
        return certificates;
    }

    public NodeEntity getNode(String nodeName) throws NodeNotFoundException {

        return this.olasDAO.getNode(nodeName);
    }

    public NodeEntity getLocalNode() throws NodeNotFoundException {

        AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
        return this.getNode(this.authenticate(authIdentityServiceClient.getCertificate()));
    }
}
