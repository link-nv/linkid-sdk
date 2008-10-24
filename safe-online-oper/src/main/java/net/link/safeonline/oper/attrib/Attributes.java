/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.attrib;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;


@Local
public interface Attributes {

    /*
     * Accessors
     */
    String getPluginConfiguration();

    void setPluginConfiguration(String pluginConfiguration);

    Long getCacheTimeout();

    void setCacheTimeout(Long cacheTimeout);

    /*
     * Factory.
     */
    void attributeTypeListFactory();

    /*
     * Actions.
     */
    String view();

    String remove();

    String removeConfirm() throws AttributeTypeDescriptionNotFoundException, PermissionDeniedException, AttributeTypeNotFoundException;

    String savePlugin() throws AttributeTypeNotFoundException;

    String saveOlas() throws AttributeTypeNotFoundException;

    /*
     * Lifecycle.
     */
    void destroyCallback();
}
