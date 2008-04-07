/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.oper.attributes;

import net.link.safeonline.webapp.oper.OperTemplate;

public class OperAttributeAddAc extends OperTemplate {

	public static final String PAGE_NAME = SAFE_ONLINE_OPER_WEBAPP_PREFIX
			+ "/attributes/attribute-add-ac.seam";

	public OperAttributeAddAc() {
		super(PAGE_NAME);
	}

	public void setUserVisible(boolean value) {
		setCheckBox("userVisible", value);
	}

	public void setUserEditable(boolean value) {
		setCheckBox("userEditable", value);
	}

	public OperAttributes add() {
		clickButtonAndWait("add");
		return new OperAttributes();
	}

	public OperAttributeAddType previous() {
		clickButtonAndWait("previous");
		return new OperAttributeAddType();
	}

	public OperAttributes cancel() {
		clickButtonAndWait("cancel");
		return new OperAttributes();
	}
}
