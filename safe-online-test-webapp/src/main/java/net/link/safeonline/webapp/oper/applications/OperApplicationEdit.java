/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.oper.applications;

import net.link.safeonline.webapp.oper.OperTemplate;

public class OperApplicationEdit extends OperTemplate {

	public static final String PAGE_NAME = SAFE_ONLINE_OPER_WEBAPP_PREFIX
			+ "/applications/application-edit.seam";

	public OperApplicationEdit() {
		super(PAGE_NAME);
	}

	public void setAttribute(String name, boolean included, boolean required,
			boolean datamining) {
		setTableRowCheckbox("identity-table", name, "included", included);
		setTableRowCheckbox("identity-table", name, "required", required);
		setTableRowCheckbox("identity-table", name, "datamining", datamining);
	}

	public OperApplicationView save() {
		clickButtonAndWait("save");
		return new OperApplicationView();
	}

	public OperApplicationView cancel() {
		clickButtonAndWait("cancel");
		return new OperApplicationView();
	}
}
