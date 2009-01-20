/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.device;

import java.io.IOException;
import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceClassNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePropertyNotFoundException;
import net.link.safeonline.authentication.exception.ExistingDeviceException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.pkix.exception.CertificateEncodingException;

import org.apache.myfaces.custom.fileupload.UploadedFile;


@Local
public interface Device {

    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "DeviceBean/local";


    /*
     * Actions
     */
    String view();

    String add()
            throws ExistingDeviceException, CertificateEncodingException, DeviceClassNotFoundException, AttributeTypeNotFoundException,
            NodeNotFoundException, IOException, PermissionDeniedException;

    String remove()
            throws DeviceNotFoundException, DeviceDescriptionNotFoundException, DevicePropertyNotFoundException;

    String edit();

    String save()
            throws DeviceNotFoundException, CertificateEncodingException, IOException, AttributeTypeNotFoundException,
            PermissionDeniedException;

    /*
     * Accessors
     */
    String getName();

    void setName(String name);

    String getDeviceClass();

    void setDeviceClass(String deviceClass);

    String getNode();

    void setNode(String node);

    String getAuthenticationPath();

    void setAuthenticationPath(String authenticationPath);

    String getAuthenticationWSPath();

    void setAuthenticationWSPath(String authenticationWSPath);

    String getRegistrationPath();

    void setRegistrationPath(String registrationPath);

    String getRemovalPath();

    void setRemovalPath(String removalPath);

    String getUpdatePath();

    void setUpdatePath(String updatePath);

    String getDisablePath();

    void setDisablePath(String disablePath);

    String getEnablePath();

    void setEnablePath(String enablePath);

    UploadedFile getCertificate();

    void setCertificate(UploadedFile certificate);

    String getAttributeType();

    void setAttributeType(String attributeType);

    String getUserAttributeType();

    void setUserAttributeType(String userAttributeType);

    String getDisableAttributeType();

    void setDisableAttributeType(String disableAttributeType);

    /*
     * Factories
     */
    void deviceListFactory();

    List<SelectItem> deviceClassesFactory();

    List<SelectItem> attributeTypesFactory();

    List<SelectItem> userAttributeTypesFactory();

    List<SelectItem> disableAttributeTypesFactory();

    List<SelectItem> nodeFactory();

    /*
     * Lifecycle.
     */
    void destroyCallback();

}
