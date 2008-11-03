/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.accounting;

import java.io.IOException;

import javax.ejb.Local;

import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.authentication.exception.PermissionDeniedException;


@Local
public interface Accounting {

    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "AccountingBean/local";

    /*
     * Accessors.
     */

    /*
     * Actions.
     */
    String view();

    String viewStat();

    String export() throws IOException;

    String exportStat() throws IOException;

    /*
     * Lifecycle.
     */
    void destroyCallback();

    void postConstructCallback();

    /*
     * Factories.
     */
    void applicationListFactory();

    void statListFactory() throws PermissionDeniedException;
}
