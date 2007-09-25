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
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Keyboard helper class.
 * 
 * @author fcorneli
 * 
 */
public class Keyboard {

	private static final Log LOG = LogFactory.getLog(Keyboard.class);

	private static final BufferedReader input = new BufferedReader(
			new InputStreamReader(System.in));

	private static final String DELIMITERS = " \t\n\r\f";

	private Keyboard() {
		// empty
	}

	public static char getNextChar() {
		StringTokenizer tokenizer;
		try {
			tokenizer = new StringTokenizer(input.readLine(), DELIMITERS, true);
		} catch (IOException e) {
			throw new RuntimeException("error");
		}
		String token = tokenizer.nextToken();
		return token.charAt(0);
	}
}