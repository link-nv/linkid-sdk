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
            if (menuAction.isActive()) {
                System.out.println(menuAction.toString());
            }
        }
        System.out.println();
        System.out.print("Make your choice:");
    }


    private static final List<AbstractMenuAction>           mainMenuActions         = new LinkedList<AbstractMenuAction>();

    static {
        mainMenuActions.add(new ConnectionMenuAction());
        mainMenuActions.add(new DisconnectMenuAction());
        mainMenuActions.add(new CheckSchemaMenuAction());
        mainMenuActions.add(new InitializeDatabaseMenuAction());
        mainMenuActions.add(new ExitMenuAction());
    }

    private static final Map<Character, AbstractMenuAction> mainMenuActivationChars = new HashMap<Character, AbstractMenuAction>();


    private static void initialize() {

        for (AbstractMenuAction menuAction : mainMenuActions) {
            char activationChar = Character.toUpperCase(menuAction.getActivationChar());
            if (mainMenuActivationChars.containsKey(activationChar))
                throw new RuntimeException("duplicate activation char: " + activationChar);
            mainMenuActivationChars.put(activationChar, menuAction);
        }
    }

    public static void main(String[] args) {

        initialize();
        while (true) {
            printMainMenu();
            char nextChar = Character.toUpperCase(Keyboard.getNextChar());
            AbstractMenuAction menuAction = mainMenuActivationChars.get(nextChar);
            if (null != menuAction) {
                if (menuAction.isActive()) {
                    menuAction.run();
                } else {
                    System.out.println("Menu action not active. Try again.");
                }
            } else {
                System.out.println("Invalid activation char. Try again.");
            }
        }
    }
}
