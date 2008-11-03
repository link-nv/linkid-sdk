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
import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.ExistingNodeException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.NodeService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.node.Node;
import net.link.safeonline.pkix.exception.CertificateEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.fileupload.UploadedFile;
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
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("operNode")
@LocalBinding(jndiBinding = Node.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class NodeBean implements Node {

    private static final Log    LOG                     = LogFactory.getLog(NodeBean.class);

    private static final String OPER_NODE_LIST_NAME     = "operOlasNodeList";

    private static final String OPER_PROTOCOL_LIST_NAME = "operProtocolList";

    private String              name;

    private String              protocol;

    private String              hostname;

    private int                 port;

    private int                 sslPort;

    private UploadedFile        authnUpFile;

    private UploadedFile        signingUpFile;

    @EJB
    private NodeService         nodeService;

    @In(create = true)
    FacesMessages               facesMessages;

    @SuppressWarnings("unused")
    @DataModel(OPER_NODE_LIST_NAME)
    private List<NodeEntity>    operNodeList;

    @DataModelSelection(OPER_NODE_LIST_NAME)
    @Out(value = "selectedNode", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private NodeEntity          selectedNode;


    @Remove
    @Destroy
    public void destroyCallback() {

        this.name = null;
        this.protocol = null;
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

    @Factory(OPER_PROTOCOL_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> protocolListFactory() {

        List<SelectItem> locations = new LinkedList<SelectItem>();
        locations.add(new SelectItem("http"));
        locations.add(new SelectItem("https"));
        return locations;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @ErrorHandling( { @Error(exceptionClass = CertificateEncodingException.class, messageId = "errorX509Encoding", fieldId = "fileupload") })
    public String add() throws CertificateEncodingException, IOException {

        LOG.debug("add olas node: " + this.name);

        try {
            byte[] encodedAuthnCertificate;
            if (null != this.authnUpFile) {
                encodedAuthnCertificate = getUpFileContent(this.authnUpFile);
            } else {
                encodedAuthnCertificate = null;
            }
            byte[] encodedSigningCertificate;
            if (null != this.signingUpFile) {
                encodedSigningCertificate = getUpFileContent(this.signingUpFile);
            } else {
                encodedSigningCertificate = null;
            }
            this.nodeService.addNode(this.name, this.protocol, this.hostname, this.port, this.sslPort, encodedAuthnCertificate,
                    encodedSigningCertificate);
        } catch (ExistingNodeException e) {
            LOG.debug("node already exists: " + this.name);
            this.facesMessages.addToControlFromResourceBundle("name", FacesMessage.SEVERITY_ERROR, "errorNodeAlreadyExists", this.name);
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

    public String getProtocol() {

        return this.protocol;
    }

    public void setProtocol(String protocol) {

        this.protocol = protocol;
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
    public String remove() throws NodeNotFoundException {

        String nodeName = this.selectedNode.getName();
        LOG.debug("remove node: " + nodeName);
        try {
            this.nodeService.removeNode(nodeName);
        } catch (PermissionDeniedException e) {
            LOG.debug("permission denied to remove: " + nodeName);
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, e.getResourceMessage(), e.getResourceArgs());
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
    public String save() throws CertificateEncodingException, NodeNotFoundException, IOException, GenericJDBCException {

        String nodeName = this.selectedNode.getName();
        LOG.debug("save node: " + nodeName);

        if (null != this.authnUpFile) {
            LOG.debug("updating node authentication certificate");
            this.nodeService.updateAuthnCertificate(nodeName, getUpFileContent(this.authnUpFile));
        }

        if (null != this.signingUpFile) {
            LOG.debug("updating node signing certificate");
            this.nodeService.updateSigningCertificate(nodeName, getUpFileContent(this.signingUpFile));
        }
        this.nodeService.updateLocation(nodeName, this.protocol, this.hostname, this.port, this.sslPort);

        /*
         * Refresh the selected application.
         */
        this.selectedNode = this.nodeService.getNode(nodeName);

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

        this.protocol = this.selectedNode.getProtocol();
        this.hostname = this.selectedNode.getHostname();
        this.port = this.selectedNode.getPort();
        this.sslPort = this.selectedNode.getSslPort();
        return "edit";
    }
}
