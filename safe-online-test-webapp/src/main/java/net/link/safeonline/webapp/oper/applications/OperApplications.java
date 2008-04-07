/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.oper.applications;

import net.link.safeonline.webapp.oper.OperTemplate;

public class OperApplications extends OperTemplate {

	public static final String PAGE_NAME = SAFE_ONLINE_OPER_WEBAPP_PREFIX
			+ "/applications/applications.seam";

	public OperApplications() {
		super(PAGE_NAME);
	}

	public OperApplicationView viewApplication(String application) {
		clickLinkInRowLinkAndWait("app-data", application, "view");
		return new OperApplicationView();
	}

	public void removeApplication(String application) {
		clickLinkInRowLinkAndWait("app-data", application, "remove");
	}
}
