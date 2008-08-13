/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.applet;

import java.net.URL;
import java.util.Locale;


/**
 * Interface definition for runtime context. In case of an applet runtime this will somehow map to the applet context.
 * 
 * @author fcorneli
 * 
 */
public interface RuntimeContext {

    /**
     * Gives back the parameter value, or <code>null</code> if not set.
     * 
     * @param name
     *            the parameter name.
     * @return the parameter value, or <code>null</code> if not set.
     */
    String getParameter(String name);

    URL getDocumentBase();

    void showDocument(URL url);

    Locale getLocale();
}
