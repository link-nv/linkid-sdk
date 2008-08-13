/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.digipass;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.model.digipass.DigipassException;


@Local
public interface Removal {

    /*
     * Accessors
     */
    String getLoginName();

    void setLoginName(String loginName);

    /*
     * Actions.
     */
    String getRegistrations() throws SubjectNotFoundException, PermissionDeniedException;

    String remove() throws SubjectNotFoundException, DigipassException, PermissionDeniedException;

    /*
     * Factories
     */
    List<AttributeDO> digipassAttributesFactory() throws SubjectNotFoundException, PermissionDeniedException;

    /*
     * Lifecycle.
     */
    void destroyCallback();
}
