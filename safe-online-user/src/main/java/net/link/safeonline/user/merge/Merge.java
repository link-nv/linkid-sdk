/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.merge;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

@Local
public interface Merge {

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Factories.
	 */
	List<SelectItem> deviceListFactory();

	/*
	 * Accessors
	 */
	void setDeviceSelection(String deviceSelection);

	String getDeviceSelection();

	void setSource(String source);

	String getSource();

	/*
	 * Actions.
	 */
	String next();

}
