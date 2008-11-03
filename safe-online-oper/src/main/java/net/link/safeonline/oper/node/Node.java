/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.node;

import java.io.IOException;
import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.pkix.exception.CertificateEncodingException;

import org.apache.myfaces.custom.fileupload.UploadedFile;


@Local
public interface Node extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/NodeBean/local";

    /*
     * Factory
     */
    void nodeListFactory();

    List<SelectItem> protocolListFactory();

    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Accessors.
     */
    String getName();

    void setName(String name);

    String getHostname();

    void setHostname(String hostname);

    String getProtocol();

    void setProtocol(String protocol);

    int getPort();

    void setPort(int port);

    int getSslPort();

    void setSslPort(int sslPort);

    void setAuthnUpFile(UploadedFile uploadedFile);

    UploadedFile getAuthnUpFile();

    void setSigningUpFile(UploadedFile uploadedFile);

    UploadedFile getSigningUpFile();

    /*
     * Actions.
     */
    String add() throws CertificateEncodingException, IOException;

    String remove() throws NodeNotFoundException;

    String save() throws CertificateEncodingException, NodeNotFoundException, IOException;

    String view();

    String edit();
}
