/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.ExistingNodeException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.NodeService;
import net.link.safeonline.authentication.service.NodeServiceRemote;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.NodeDAO;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.pkix.PkiUtils;
import net.link.safeonline.pkix.exception.CertificateEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;


/**
 * Implementation of node service interface.
 *
 * @author wvdhaute
 *
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class NodeServiceBean implements NodeService, NodeServiceRemote {

    private static final Log LOG = LogFactory.getLog(NodeServiceBean.class);

    @EJB
    private NodeDAO          olasDAO;

    @EJB
    private AttributeTypeDAO attributeTypeDAO;


    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<NodeEntity> listNodes() {

        return this.olasDAO.listNodes();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void addNode(String name, String protocol, String hostname, int port, int sslPort,
            byte[] encodedAuthnCertificate, byte[] encodedSigningCertificate) throws ExistingNodeException,
            CertificateEncodingException {

        LOG.debug("add olas node: " + name);
        checkExistingNode(name);

        X509Certificate authnCertificate = PkiUtils.decodeCertificate(encodedAuthnCertificate);
        X509Certificate signingCertificate = PkiUtils.decodeCertificate(encodedSigningCertificate);

        this.olasDAO.addNode(name, protocol, hostname, port, sslPort, authnCertificate, signingCertificate);
    }

    private void checkExistingNode(String name) throws ExistingNodeException {

        NodeEntity existingNode = this.olasDAO.findNode(name);
        if (null != existingNode) {
            throw new ExistingNodeException();
        }
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void removeNode(String name) throws NodeNotFoundException, PermissionDeniedException {

        LOG.debug("remove node: " + name);
        NodeEntity node = this.olasDAO.getNode(name);

        // check if present in an attribute type
        List<AttributeTypeEntity> nodeAttributeTypes = this.attributeTypeDAO.listAttributeTypes(node);
        if (nodeAttributeTypes.size() > 0) {
            throw new PermissionDeniedException("Still attribute types attached to this node",
                    "errorPermissionNodeHasAttributes");
        }

        this.olasDAO.removeNode(node);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public NodeEntity getNode(String nodeName) throws NodeNotFoundException {

        return this.olasDAO.getNode(nodeName);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateAuthnCertificate(String nodeName, byte[] certificateData) throws CertificateEncodingException,
            NodeNotFoundException {

        LOG.debug("updating olas node authentication certificate for " + nodeName);
        X509Certificate certificate = PkiUtils.decodeCertificate(certificateData);

        NodeEntity node = this.olasDAO.getNode(nodeName);
        node.setAuthnCertificate(certificate);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateSigningCertificate(String nodeName, byte[] certificateData) throws CertificateEncodingException,
            NodeNotFoundException {

        LOG.debug("updating olas node certificate for " + nodeName);
        X509Certificate certificate = PkiUtils.decodeCertificate(certificateData);

        NodeEntity node = this.olasDAO.getNode(nodeName);
        node.setSigningCertificate(certificate);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateLocation(String nodeName, String protocol, String hostname, int port, int sslPort)
            throws NodeNotFoundException {

        LOG.debug("update olas node location for " + nodeName);
        NodeEntity node = this.olasDAO.getNode(nodeName);
        node.setProtocol(protocol);
        node.setHostname(hostname);
        node.setPort(port);
        node.setSslPort(sslPort);
    }
}
