/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.taglib;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

public class AttributeComponentUtil {

	public static final String RESOURCE_BASE = "net.link.safeonline.taglib.Messages";

	private AttributeComponentUtil() {
		// empty
	}

	public static ResourceBundle getResourceBundle(FacesContext context) {
		Locale locale = context.getExternalContext().getRequestLocale();
		ResourceBundle messages = ResourceBundle.getBundle(RESOURCE_BASE,
				locale);
		return messages;
	}
}
