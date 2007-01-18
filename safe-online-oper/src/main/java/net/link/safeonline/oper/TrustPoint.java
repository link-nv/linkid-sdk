/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper;

import javax.ejb.Local;

import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.myfaces.custom.tree2.TreeModel;

@Local
public interface TrustPoint {

	void destroyCallback();

	TreeModel getTreeModel();

	void setUpFile(UploadedFile uploadedFile);

	UploadedFile getUpFile();

	String add();

	String view();

	String removeTrustPoint();
}
