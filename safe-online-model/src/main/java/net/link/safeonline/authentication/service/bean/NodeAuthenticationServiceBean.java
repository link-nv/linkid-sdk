/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.cert.X509Certificate;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.dao.OlasDAO;
import net.link.safeonline.entity.OlasEntity;
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

	private static final Log LOG = LogFactory
			.getLog(NodeAuthenticationServiceBean.class);

	@EJB
	private OlasDAO olasDAO;

	public String authenticate(X509Certificate authnCertificate)
			throws NodeNotFoundException {
		OlasEntity node = this.olasDAO
				.getNodeFromAuthnCertificate(authnCertificate);
		String nodeName = node.getName();
		LOG.debug("authenticated node: " + nodeName);
		return nodeName;
	}

	public X509Certificate getAuthnCertificate(String nodeName)
			throws NodeNotFoundException {
		LOG.debug("get authentication certificate for node: " + nodeName);
		OlasEntity node = this.olasDAO.getNode(nodeName);
		X509Certificate authnCertificate = node.getAuthnCertificate();
		return authnCertificate;
	}

	public X509Certificate getSigningCertificate(String nodeName)
			throws NodeNotFoundException {
		LOG.debug("get signing certificate for node: " + nodeName);
		OlasEntity node = this.olasDAO.getNode(nodeName);
		X509Certificate signingCertificate = node.getSigningCertificate();
		return signingCertificate;
	}

	public OlasEntity getNode(String nodeName) throws NodeNotFoundException {
		return this.olasDAO.getNode(nodeName);
	}

	public OlasEntity getLocalNode() throws NodeNotFoundException {
		AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
		return this.getNode(this.authenticate(authIdentityServiceClient
				.getCertificate()));
	}
}
