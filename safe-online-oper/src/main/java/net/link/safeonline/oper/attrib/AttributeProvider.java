/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.attrib;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeProviderNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeProviderException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;


@Local
public interface AttributeProvider {

    /*
     * factories
     */
    void attributeProvidersFactory() throws AttributeTypeNotFoundException;

    List<SelectItem> getApplicationList();

    /*
     * actions
     */
    String removeProvider() throws AttributeTypeNotFoundException, AttributeProviderNotFoundException;

    String add() throws ExistingAttributeProviderException, ApplicationNotFoundException, AttributeTypeNotFoundException,
                PermissionDeniedException;

    /*
     * accessors
     */
    String getApplication();

    void setApplication(String application);

    /*
     * lifecycle
     */
    void destroyCallback();
}
