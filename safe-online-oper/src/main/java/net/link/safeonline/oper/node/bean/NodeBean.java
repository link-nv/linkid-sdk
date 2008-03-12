/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.node.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;

import net.link.safeonline.authentication.exception.ExistingNodeException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.NodeService;
import net.link.safeonline.entity.OlasEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.node.Node;
import net.link.safeonline.pkix.exception.CertificateEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.UploadedFile;
import org.hibernate.exception.GenericJDBCException;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("operNode")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX + "NodeBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class NodeBean implements Node {

	private static final Log LOG = LogFactory.getLog(NodeBean.class);

	private static final String OPER_NODE_LIST_NAME = "operOlasNodeList";

	private String name;

	private String hostname;

	private int port;

	private int sslPort;

	private UploadedFile authnUpFile;

	private UploadedFile signingUpFile;

	@EJB
	private NodeService nodeService;

	@In(create = true)
	FacesMessages facesMessages;

	@SuppressWarnings("unused")
	@DataModel(OPER_NODE_LIST_NAME)
	private List<OlasEntity> operNodeList;

	@DataModelSelection(OPER_NODE_LIST_NAME)
	@Out(value = "selectedNode", required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private OlasEntity selectedNode;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.name = null;
		this.hostname = null;
		this.authnUpFile = null;
		this.signingUpFile = null;
	}

	@Factory(OPER_NODE_LIST_NAME)
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void nodeListFactory() {
		LOG.debug("node list factory");
		this.operNodeList = this.nodeService.listNodes();
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String add() {
		LOG.debug("add olas node: " + this.name);

		try {
			byte[] encodedAuthnCertificate;
			if (null != this.authnUpFile)
				encodedAuthnCertificate = getUpFileContent(this.authnUpFile);
			else
				encodedAuthnCertificate = null;
			byte[] encodedSigningCertificate;
			if (null != this.signingUpFile)
				encodedSigningCertificate = getUpFileContent(this.signingUpFile);
			else
				encodedSigningCertificate = null;
			this.nodeService.addNode(this.name, this.hostname, this.port,
					this.sslPort, encodedAuthnCertificate,
					encodedSigningCertificate);
		} catch (ExistingNodeException e) {
			LOG.debug("node already exists: " + this.name);
			this.facesMessages.addToControlFromResourceBundle("name",
					FacesMessage.SEVERITY_ERROR, "errorNodeAlreadyExists",
					this.name);
			return null;
		} catch (IOException e) {
			LOG.debug("IO error");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorIO");
			return null;
		} catch (CertificateEncodingException e) {
			LOG.debug("X509 certificate encoding error");
			this.facesMessages.addToControlFromResourceBundle("fileupload",
					FacesMessage.SEVERITY_ERROR, "errorX509Encoding");
			return null;
		}
		nodeListFactory();
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public UploadedFile getAuthnUpFile() {
		return this.authnUpFile;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setAuthnUpFile(UploadedFile uploadedFile) {
		this.authnUpFile = uploadedFile;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public UploadedFile getSigningUpFile() {
		return this.signingUpFile;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setSigningUpFile(UploadedFile uploadedFile) {
		this.signingUpFile = uploadedFile;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHostname() {
		return this.hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getSslPort() {
		return this.sslPort;
	}

	public void setSslPort(int sslPort) {
		this.sslPort = sslPort;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String remove() {
		String nodeName = this.selectedNode.getName();
		LOG.debug("remove node: " + nodeName);
		try {
			this.nodeService.removeNode(nodeName);
		} catch (NodeNotFoundException e) {
			LOG.debug("node not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorNodeNotFound");
			return null;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied to remove: " + nodeName);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}
		nodeListFactory();
		return "success";
	}

	private byte[] getUpFileContent(UploadedFile file) throws IOException {
		InputStream inputStream = file.getInputStream();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		IOUtils.copy(inputStream, byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String save() {
		String nodeName = this.selectedNode.getName();
		LOG.debug("save node: " + nodeName);

		if (null != this.authnUpFile) {
			LOG.debug("updating node authentication certificate");
			try {
				this.nodeService.updateAuthnCertificate(nodeName,
						getUpFileContent(this.authnUpFile));
			} catch (CertificateEncodingException e) {
				LOG.debug("certificate encoding error");
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "errorX509Encoding");
				return null;
			} catch (NodeNotFoundException e) {
				LOG.debug("node not found");
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "errorNodeNotFound");
				return null;
			} catch (IOException e) {
				LOG.debug("IO error: " + e.getMessage());
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "errorIO");
				return null;
			}
		}

		if (null != this.signingUpFile) {
			LOG.debug("updating node signing certificate");
			try {
				this.nodeService.updateSigningCertificate(nodeName,
						getUpFileContent(this.signingUpFile));
			} catch (CertificateEncodingException e) {
				LOG.debug("certificate encoding error");
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "errorX509Encoding");
				return null;
			} catch (NodeNotFoundException e) {
				LOG.debug("node not found");
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "errorNodeNotFound");
				return null;
			} catch (IOException e) {
				LOG.debug("IO error: " + e.getMessage());
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "errorIO");
				return null;
			}
		}
		try {
			this.nodeService.updateLocation(nodeName, this.hostname, this.port,
					this.sslPort);

			/*
			 * Refresh the selected application.
			 */
			this.selectedNode = this.nodeService.getNode(nodeName);
		} catch (GenericJDBCException e) {
			LOG.debug("invalid data type.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDataType");
			return null;
		} catch (NodeNotFoundException e) {
			LOG.debug("node not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorNodeNotFound");
			return null;
		}

		nodeListFactory();
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String view() {
		/*
		 * To set the selected node.
		 */
		LOG.debug("view node: " + this.selectedNode.getName());
		return "view";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String edit() {
		/*
		 * To set the selected application.
		 */
		LOG.debug("edit application: " + this.selectedNode.getName());

		this.hostname = this.selectedNode.getHostname();
		this.port = this.selectedNode.getPort();
		this.sslPort = this.selectedNode.getSslPort();
		return "edit";
	}
}
