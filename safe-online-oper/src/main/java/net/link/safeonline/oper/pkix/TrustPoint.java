/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.pkix;

import java.io.IOException;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.pkix.exception.CertificateEncodingException;
import net.link.safeonline.pkix.exception.ExistingTrustPointException;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.exception.TrustPointNotFoundException;

import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.myfaces.custom.tree2.TreeModel;


@Local
public interface TrustPoint extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/TrustPointBean/local";

    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Accessors.
     */
    TreeModel getTreeModel();

    void setUpFile(UploadedFile uploadedFile);

    UploadedFile getUpFile();

    /*
     * Actions.
     */
    String add() throws IOException, TrustDomainNotFoundException, ExistingTrustPointException, CertificateEncodingException;

    String view();

    String removeTrustPoint() throws TrustPointNotFoundException;
}
