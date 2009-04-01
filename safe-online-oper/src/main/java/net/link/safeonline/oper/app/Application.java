/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.app;

import java.io.IOException;
import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.pkix.exception.CertificateEncodingException;

import org.apache.myfaces.custom.fileupload.UploadedFile;


@Local
public interface Application {

    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "ApplicationBean/local";


    /*
     * Factory
     */
    void applicationListFactory()
            throws ApplicationNotFoundException;

    void newIdentityAttributesFactory();

    void identityAttributesFactory()
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, PermissionDeniedException;

    void applicationIdentityAttributesFactory()
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, PermissionDeniedException;

    List<SelectItem> availableApplicationOwnersFactory();

    List<SelectItem> appliactionIdScopeFactory();

    void allowedDevices();

    void usageAgreementListFactory()
            throws ApplicationNotFoundException, PermissionDeniedException;

    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Accessors.
     */
    String getName();

    void setName(String name);

    String getFriendlyName();

    void setFriendlyName(String friendlyName);

    String getDescription();

    void setDescription(String description);

    String getApplicationUrl();

    void setApplicationUrl(String applicationUrl);

    byte[] getApplicationLogo();

    void setApplicationLogo(byte[] applicationLogo);

    UploadedFile getApplicationLogoFile();

    void setApplicationLogoFile(UploadedFile applicationLogoFile);

    String getApplicationOwner();

    void setApplicationOwner(String applicationOwner);

    void setUpFile(UploadedFile uploadedFile);

    UploadedFile getUpFile();

    boolean isIdmapping();

    void setIdmapping(boolean idmapping);

    String getUsageAgreement()
            throws ApplicationNotFoundException;

    void setApplicationIdScope(String applicationIdScope);

    String getApplicationIdScope();

    boolean isSkipMessageIntegrityCheck();

    void setSkipMessageIntegrityCheck(boolean skipMessageIntegrityCheck);

    boolean isDeviceRestriction();

    void setDeviceRestriction(boolean deviceRestriction);

    boolean isSsoEnabled();

    void setSsoEnabled(boolean ssoEnabled);

    String getSsoLogoutUrl();

    void setSsoLogoutUrl(String ssoLogoutUrl);

    Long getSessionTimeout();

    void setSessionTimeout(Long sessionTimeout);

    /*
     * Actions.
     */
    String add()
            throws AttributeTypeNotFoundException, IOException, ApplicationNotFoundException;

    String removeApplication()
            throws ApplicationNotFoundException;

    String save()
            throws CertificateEncodingException, ApplicationNotFoundException, IOException, ApplicationIdentityNotFoundException,
            AttributeTypeNotFoundException, PermissionDeniedException, ApplicationOwnerNotFoundException;

    String view();

    String edit();

    String viewUsageAgreement();

    String editUsageAgreement()
            throws ApplicationNotFoundException, PermissionDeniedException;
}
