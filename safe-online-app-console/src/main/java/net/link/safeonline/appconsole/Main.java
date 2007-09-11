/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import java.awt.EventQueue;

import javax.swing.UIManager;

import net.java.javafx.FXShell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SafeOnline Application Console entry point.
 * 
 * @author fcorneli
 * 
 */
public class Main {

	private static final Log LOG = LogFactory.getLog(Main.class);

	public static void main(String[] args) {
		startSwingConsole();
		// startJavaFXConsole();
	}

	public static void startSwingConsole() {
		LOG.info("Starting Swing SafeOnline Application Console...");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		Runnable runner = new Runnable() {
			public void run() {
				new ApplicationConsole();
			}
		};
		EventQueue.invokeLater(runner);
	}

	public static void startJavaFXConsole() {
		LOG.info("Starting Java FX SafeOnline Application Console...");
		try {
			FXShell
					.main(new String[] { "net/link/safeonline/appconsole/Main.fx" });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}