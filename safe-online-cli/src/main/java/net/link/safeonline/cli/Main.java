/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SafeOnline Command Line Interface Entry Point.
 * 
 * @author fcorneli
 * 
 */
public class Main {

	private static void printMainMenu() {
		System.out.println("SafeOnline CLI - Main Menu");
		System.out.println();
		for (AbstractMenuAction menuAction : mainMenuActions) {
			System.out.println(menuAction.toString());
		}
		System.out.println();
		System.out.print("Make you choice:");
	}

	private static final List<AbstractMenuAction> mainMenuActions = new LinkedList<AbstractMenuAction>();

	static {
		mainMenuActions.add(new ConnectionMenuAction());
		mainMenuActions.add(new ExitMenuAction());
	}

	private static final Map<Character, AbstractMenuAction> mainMenuActivationChars = new HashMap<Character, AbstractMenuAction>();

	private static void initialize() {
		for (AbstractMenuAction menuAction : mainMenuActions) {
			char activationChar = menuAction.getActivationChar();
			if (mainMenuActivationChars.containsKey(activationChar)) {
				throw new RuntimeException("duplicate activation char: "
						+ activationChar);
			}
			mainMenuActivationChars.put(activationChar, menuAction);
		}
	}

	public static void main(String[] args) {
		initialize();
		while (true) {
			printMainMenu();
			char nextChar = Keyboard.getNextChar();
			AbstractMenuAction menuAction = mainMenuActivationChars
					.get(nextChar);
			if (null != menuAction) {
				menuAction.run();
			} else {
				System.out.println("Invalid activation char. Try again.");
			}
		}
	}
}
