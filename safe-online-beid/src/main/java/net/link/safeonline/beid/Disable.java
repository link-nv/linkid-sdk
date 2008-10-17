/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid;

import java.io.IOException;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


@Local
public interface Disable {

    /*
     * Accessors.
     */
    /*
     * Actions.
     */
    String save() throws IOException, SubjectNotFoundException, AttributeNotFoundException;

    String cancel() throws IOException;

    /*
     * Factories
     */
    List<Registration> registrationsFactory() throws SubjectNotFoundException, DeviceNotFoundException;

    /*
     * Lifecycle.
     */
    void destroyCallback();
}
