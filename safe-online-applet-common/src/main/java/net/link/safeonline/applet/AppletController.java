/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.applet;

/**
 * Interface for applet controller components.
 * 
 * @author fcorneli
 * 
 */
public interface AppletController extends Runnable {

    /**
     * Initializes the controller component. Thus injects its dependencies.
     * 
     * @param appletView
     * @param runtimeContext
     * @param statementProvider
     */
    void init(AppletView appletView, RuntimeContext runtimeContext, StatementProvider statementProvider);

    void abort();
}
