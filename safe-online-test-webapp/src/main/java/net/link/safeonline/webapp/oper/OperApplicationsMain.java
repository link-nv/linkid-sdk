/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.oper;

import net.link.safeonline.webapp.oper.applications.OperApplications;
import net.link.safeonline.webapp.oper.owners.OperOwners;

public class OperApplicationsMain extends OperTemplate {

	public static final String PAGE_NAME = SAFE_ONLINE_OPER_WEBAPP_PREFIX
			+ "/applications-main.seam";

	public OperApplicationsMain() {
		super(PAGE_NAME);
	}

	public OperApplications gotoApplications() {
		clickLinkAndWait("goto_applications");
		return new OperApplications();
	}

	public OperOwners gotoOwners() {
		clickLinkAndWait("owners");
		return new OperOwners();
	}
}
