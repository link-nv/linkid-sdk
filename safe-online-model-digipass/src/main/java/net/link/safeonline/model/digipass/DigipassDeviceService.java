/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.digipass;

import java.util.List;
import java.util.Locale;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.data.AttributeDO;


@Local
public interface DigipassDeviceService {

    String authenticate(String loginName, String token) throws SubjectNotFoundException, PermissionDeniedException,
            DeviceNotFoundException;

    String register(String loginName, String serialNumber) throws ArgumentIntegrityException, SubjectNotFoundException,
            PermissionDeniedException;

    void remove(String loginName, String serialNumber) throws SubjectNotFoundException, DigipassException,
            PermissionDeniedException, DeviceNotFoundException;

    List<AttributeDO> getDigipasses(String loginName, Locale locale) throws SubjectNotFoundException,
            PermissionDeniedException, DeviceNotFoundException;
}
