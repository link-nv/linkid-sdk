/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.awt.EventQueue;

import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Siemens Acceptance Test Console for WS Authentication.
 * 
 * @author wvdhaute
 * 
 */
public class Main {

    private static final Log LOG = LogFactory.getLog(Main.class);


    public static void main(String[] args) {

        startSwingConsole();
    }

    public static void startSwingConsole() {

        LOG.info("Starting Swing Siemens Acceptance Test Console...");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOG.error("exception: " + e.getMessage(), e);
        }
        Runnable runner = new Runnable() {

            public void run() {

                new AcceptanceConsole();
            }
        };
        EventQueue.invokeLater(runner);
    }
}
