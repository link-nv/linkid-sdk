/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePropertyNotFoundException;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceDescriptionEntity;
import net.link.safeonline.entity.DeviceDescriptionPK;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DevicePropertyEntity;
import net.link.safeonline.entity.DevicePropertyPK;
import net.link.safeonline.entity.NodeEntity;


@Local
public interface DeviceDAO {

    DeviceEntity addDevice(String name, DeviceClassEntity deviceClass, NodeEntity node, String authenticationPath,
            String registrationPath, String removalPath, String updatePath, String disablePath,
            X509Certificate certificate, AttributeTypeEntity attributeType, AttributeTypeEntity userAttributeType,
            AttributeTypeEntity disableAttributeType);

    List<DeviceEntity> listDevices();

    List<DeviceEntity> listDevices(DeviceClassEntity deviceClass);

    DeviceEntity findDevice(String name);

    DeviceEntity getDevice(String name) throws DeviceNotFoundException;

    void removeDevice(String name);

    List<DeviceDescriptionEntity> listDescriptions(DeviceEntity device);

    List<DevicePropertyEntity> listProperties(DeviceEntity device);

    void addDescription(DeviceEntity device, DeviceDescriptionEntity description);

    void removeDescription(DeviceDescriptionEntity description);

    void saveDescription(DeviceDescriptionEntity description);

    DeviceDescriptionEntity getDescription(DeviceDescriptionPK descriptionPK) throws DeviceDescriptionNotFoundException;

    DeviceDescriptionEntity findDescription(DeviceDescriptionPK descriptionPK);

    void addProperty(DeviceEntity device, DevicePropertyEntity property);

    void removeProperty(DevicePropertyEntity property);

    void saveProperty(DevicePropertyEntity property);

    DevicePropertyEntity getProperty(DevicePropertyPK propertyPK) throws DevicePropertyNotFoundException;

    DevicePropertyEntity findProperty(DevicePropertyPK propertyPK);

    List<DeviceEntity> listDevices(String authenticationContextClass);

    DeviceEntity getDevice(X509Certificate certificate) throws DeviceNotFoundException;

    DeviceEntity findDevice(X509Certificate certificate);
}
