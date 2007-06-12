/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.FacesContext;

import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.TrustPoint;
import net.link.safeonline.pkix.exception.CertificateEncodingException;
import net.link.safeonline.pkix.exception.ExistingTrustPointException;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.exception.TrustPointNotFoundException;
import net.link.safeonline.pkix.service.PkiService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.tree2.TreeModel;
import org.apache.myfaces.custom.tree2.TreeModelBase;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.apache.myfaces.custom.tree2.TreeNodeBase;
import org.apache.myfaces.trinidad.model.UploadedFile;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("trustPoint")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "TrustPointBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class TrustPointBean implements TrustPoint {

	private static final Log LOG = LogFactory.getLog(TrustPointBean.class);

	private UploadedFile upFile;

	public static final String DEFAULT_NODE_TYPE = "node";

	public static final String ROOT_NODE_TYPE = "root";

	@In(value = "selectedTrustDomain")
	private TrustDomainEntity selectedTrustDomain;

	@EJB
	private PkiService pkiService;

	@In(create = true)
	FacesMessages facesMessages;

	@Remove
	@Destroy
	public void destroyCallback() {
		// empty
	}

	@SuppressWarnings("unchecked")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public TreeModel getTreeModel() {
		LOG.debug("get tree model for domain: " + this.selectedTrustDomain);
		String domainName = this.selectedTrustDomain.getName();
		List<TrustPointEntity> trustPoints;
		try {
			trustPoints = this.pkiService.listTrustPoints(domainName);
		} catch (TrustDomainNotFoundException e) {
			LOG.error("trust domain not found");
			TreeNode rootNode = new TreeNodeBase(ROOT_NODE_TYPE,
					"ERROR: trust domain not found", true);
			TreeModel treeModel = new TreeModelBase(rootNode);
			return treeModel;
		}
		TreeNode rootNode = new TreeNodeBase(ROOT_NODE_TYPE, domainName, false);
		TreeModel treeModel = new TreeModelBase(rootNode);

		HashMap<String, TreeNode> nodes = new HashMap<String, TreeNode>();

		for (TrustPointEntity trustPoint : trustPoints) {
			String nodeDescription = trustPoint.getPk().getSubjectName();
			LOG.debug("adding node: " + nodeDescription);
			TreeNode newNode = new TrustPointTreeNode(DEFAULT_NODE_TYPE,
					nodeDescription, true, trustPoint);
			TreeNode parentNode = nodes.get(trustPoint.getIssuerName());
			if (null != parentNode) {
				LOG.debug("to parent node: " + parentNode.getDescription());
				parentNode.getChildren().add(newNode);
				parentNode.setLeaf(false);
			} else {
				LOG.debug("to root node: " + rootNode.getDescription());
				rootNode.getChildren().add(newNode);
				rootNode.setLeaf(false);
			}
			// be careful for self-signed certs here
			nodes.put(nodeDescription, newNode);
		}
		return treeModel;
	}

	private byte[] getUpFileContent() throws IOException {
		InputStream inputStream = this.upFile.getInputStream();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		IOUtils.copy(inputStream, byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String add() {
		String domainName = this.selectedTrustDomain.getName();
		LOG.debug("adding trust point to domain " + domainName);
		try {
			byte[] content = getUpFileContent();
			this.pkiService.addTrustPoint(domainName, content);
		} catch (TrustDomainNotFoundException e) {
			String msg = "trust domain not found";
			LOG.debug(msg);
			this.facesMessages.addToControl("fileupload", msg);
			return null;
		} catch (CertificateEncodingException e) {
			String msg = "certificate encoding error";
			LOG.debug(msg);
			this.facesMessages.addToControl("fileupload", msg);
			return null;
		} catch (ExistingTrustPointException e) {
			String msg = "existing trust point";
			LOG.debug(msg);
			this.facesMessages.addToControl("fileupload", msg);
			return null;
		} catch (IOException e) {
			String msg = "I/O error";
			LOG.debug(msg);
			this.facesMessages.addToControl("fileupload", msg);
			return null;
		}
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public UploadedFile getUpFile() {
		return this.upFile;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setUpFile(UploadedFile uploadedFile) {
		this.upFile = uploadedFile;
	}

	@SuppressWarnings("unchecked")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String view() {
		TrustPointTreeNode selectedNode = (TrustPointTreeNode) FacesContext
				.getCurrentInstance().getExternalContext().getRequestMap().get(
						"node");
		TrustPointEntity selectedTrustPoint = selectedNode.getTrustPoint();
		LOG.debug("view: " + selectedTrustPoint);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put("selectedTrustPoint", selectedTrustPoint);
		return "view";
	}

	public static class TrustPointTreeNode extends TreeNodeBase {

		private static final long serialVersionUID = 1L;

		private final TrustPointEntity trustPoint;

		public TrustPointTreeNode(String type, String description,
				boolean leaf, TrustPointEntity trustPoint) {
			super(type, description, leaf);
			this.trustPoint = trustPoint;
		}

		public TrustPointEntity getTrustPoint() {
			return this.trustPoint;
		}

		public BigInteger getSerialNumber() {
			return this.trustPoint.getCertificate().getSerialNumber();
		}
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String removeTrustPoint() {
		TrustPointEntity selectedTrustPoint = (TrustPointEntity) FacesContext
				.getCurrentInstance().getExternalContext().getSessionMap().get(
						"selectedTrustPoint");
		LOG.debug("remove trust point: " + selectedTrustPoint);
		try {
			this.pkiService.removeTrustPoint(selectedTrustPoint);
		} catch (TrustPointNotFoundException e) {
			String msg = "trust point not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		return "removed";
	}
}
