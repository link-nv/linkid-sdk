/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import java.util.ResourceBundle;

public enum Messages {

	TITLE("title");

	private static final String MESSAGES_RESOURCE = "net.link.safeonline.appconsole.messages";

	private final ResourceBundle messages = ResourceBundle
			.getBundle(MESSAGES_RESOURCE);

	private final String key;

	Messages(String key) {
		this.key = key;
	}

	public String getMessage() {
		return messages.getString(this.key);
	}
}