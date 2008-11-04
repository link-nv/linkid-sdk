/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.attrib;

import javax.ejb.Local;

import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;


@Local
public interface AttributeDescription {

    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "AttributeDescriptionBean/local";


    /*
     * Factories.
     */
    void attributeTypeDescriptionsFactory()
            throws AttributeTypeNotFoundException;

    AttributeTypeDescriptionEntity newAttributeTypeDescriptionFactory();

    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Actions.
     */
    String edit();

    String add()
            throws AttributeTypeNotFoundException;

    String save();

    String removeDescription()
            throws AttributeTypeDescriptionNotFoundException, AttributeTypeNotFoundException;

    String cancelEdit();
}
