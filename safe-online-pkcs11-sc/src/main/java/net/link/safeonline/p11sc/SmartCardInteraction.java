/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc;

import java.util.Locale;


/**
 * Smart Card Interaction interface. Via this interface the smart card component can interact with the end-user.
 * 
 * @author fcorneli
 */
public interface SmartCardInteraction {

    /**
     * Output a message to the end-user.
     * 
     * @param message
     *            the message to be displayed.
     */
    void output(String message);

    /**
     * Gives back the locale in which we should communicate to the end-user.
     * 
     * @return
     */
    Locale getLocale();
}
