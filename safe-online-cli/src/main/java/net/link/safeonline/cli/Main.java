/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

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
		System.out.println("C. Connect to Database");
		System.out.println("V. Verify the Database");
		System.out.println("I. Initialize the Database");
		System.out.println("E. Exit CLI");
		System.out.println();
		System.out.print("Make you choice:");
	}

	public static void main(String[] args) {
		char nextChar = Keyboard.getNextChar();
		do {
			printMainMenu();
		} while (nextChar != 'e');
	}
}
