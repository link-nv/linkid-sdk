/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoginSessionListener implements HttpSessionAttributeListener {

	private static final Log LOG = LogFactory
			.getLog(LoginSessionListener.class);

	public void attributeAdded(HttpSessionBindingEvent event) {
		String attributeName = event.getName();
		if ("username".equals(attributeName)) {
			String username = (String) event.getValue();
			LOG.debug("attribute username added: " + username);
		}
	}

	public void attributeRemoved(HttpSessionBindingEvent event) {
		String attributeName = event.getName();
		if ("username".equals(attributeName)) {
			String username = (String) event.getValue();
			LOG.debug("attribute username removed: " + username);
		}
	}

	public void attributeReplaced(HttpSessionBindingEvent event) {
		String attributeName = event.getName();
		if ("username".equals(attributeName)) {
			String username = (String) event.getValue();
			LOG.debug("attribute username replaced: " + username);
		}
	}
}
