/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;


/**
 * Keyboard helper class.
 * 
 * @author fcorneli
 * 
 */
public class Keyboard {

    private static final BufferedReader input      = new BufferedReader(new InputStreamReader(System.in));

    private static final String         DELIMITERS = " \t\n\r\f";


    private Keyboard() {

        // empty
    }

    private static String getNextToken() {

        StringTokenizer tokenizer;
        try {
            tokenizer = new StringTokenizer(input.readLine(), DELIMITERS, true);
        } catch (IOException e) {
            throw new RuntimeException("error: " + e.getMessage(), e);
        }
        String token = tokenizer.nextToken();
        return token;
    }

    public static char getNextChar() {

        while (true) {
            try {
                String token = getNextToken();
                char ch = token.charAt(0);
                return ch;
            } catch (NoSuchElementException e) {
                // retry
            }
        }
    }

    public static String getString() {

        String line;
        try {
            line = input.readLine();
        } catch (IOException e) {
            throw new RuntimeException("error: " + e.getMessage(), e);
        }
        return line;
    }

    public static Integer getInteger() {

        while (true) {
            String line = getString();
            try {
                int result = Integer.parseInt(line);
                return result;
            } catch (NumberFormatException e) {
                System.out.println("Input value is not an integer. Try again.");
            }
        }
    }
}
