/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.helpdesk;

import net.link.safeonline.webapp.user.UserTemplate;

public class HelpdeskLogView extends UserTemplate {

	public static final String PAGE_NAME = SAFE_ONLINE_HELPDESK_WEBAPP_PREFIX
			+ "/log-view.seam";

	public HelpdeskLogView() {
		super(PAGE_NAME);
	}
}
