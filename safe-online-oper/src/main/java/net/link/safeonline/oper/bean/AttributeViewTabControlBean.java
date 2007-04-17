/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.bean;

import java.io.Serializable;

/**
 * Attribute View Tab Controller Bean. This bean keeps track of the visible tab.
 * Notice this is not a Seam managed bean but a regular Java Bean. Tomahawk and
 * Seam don't get along that well.
 * 
 * @author fcorneli
 * 
 */
public class AttributeViewTabControlBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean descriptionVisible = true;

	private boolean genericVisible = true;

	private boolean providersVisible = true;

	public boolean isDescriptionVisible() {
		return this.descriptionVisible;
	}

	public boolean isGenericVisible() {
		return this.genericVisible;
	}

	public boolean isProvidersVisible() {
		return this.providersVisible;
	}

	public void setDescriptionVisible(boolean visible) {
		this.descriptionVisible = visible;
	}

	public void setGenericVisible(boolean visible) {
		this.genericVisible = visible;
	}

	public void setProvidersVisible(boolean visible) {
		this.providersVisible = visible;
	}
}
