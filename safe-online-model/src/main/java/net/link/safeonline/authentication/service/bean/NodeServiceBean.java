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
import net.link.safeonline.dao.OlasDAO;
import net.link.safeonline.entity.OlasEntity;
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
	private OlasDAO olasDAO;

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<OlasEntity> listNodes() {
		return this.olasDAO.listNodes();
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void addNode(String name, String location, byte[] encodedCertificate)
			throws ExistingNodeException, CertificateEncodingException {
		LOG.debug("add olas node: " + name);
		checkExistingNode(name);

		X509Certificate certificate = PkiUtils
				.decodeCertificate(encodedCertificate);

		this.olasDAO.addNode(name, location, certificate);
	}

	private void checkExistingNode(String name) throws ExistingNodeException {
		OlasEntity existingNode = this.olasDAO.findNode(name);
		if (null != existingNode)
			throw new ExistingNodeException();
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeNode(String name) throws NodeNotFoundException,
			PermissionDeniedException {
		LOG.debug("remove node: " + name);
		OlasEntity node = this.olasDAO.getNode(name);
		this.olasDAO.removeNode(node);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public OlasEntity getNode(String nodeName) throws NodeNotFoundException {
		return this.olasDAO.getNode(nodeName);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void updateCertificate(String nodeName, byte[] certificateData)
			throws CertificateEncodingException, NodeNotFoundException {
		LOG.debug("updating olas node certificate for " + nodeName);
		X509Certificate certificate = PkiUtils
				.decodeCertificate(certificateData);

		OlasEntity node = this.olasDAO.getNode(nodeName);
		node.setCertificate(certificate);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void updateLocation(String nodeName, String location)
			throws NodeNotFoundException {
		LOG.debug("update olas node location for " + nodeName);
		OlasEntity node = this.olasDAO.getNode(nodeName);
		node.setLocation(location);
	}
}
