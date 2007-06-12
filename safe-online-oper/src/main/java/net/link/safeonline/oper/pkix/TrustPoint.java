/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.pkix;

import javax.ejb.Local;

import org.apache.myfaces.custom.tree2.TreeModel;
import org.apache.myfaces.trinidad.model.UploadedFile;

@Local
public interface TrustPoint {

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Accessors.
	 */
	TreeModel getTreeModel();

	void setUpFile(UploadedFile uploadedFile);

	UploadedFile getUpFile();

	/*
	 * Actions.
	 */
	String add();

	String view();

	String removeTrustPoint();
}
