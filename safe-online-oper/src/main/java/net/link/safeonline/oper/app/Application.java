/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.app;

import javax.ejb.Local;

import org.apache.myfaces.custom.fileupload.UploadedFile;

@Local
public interface Application {

	/*
	 * Factory
	 */
	void applicationListFactory();

	void newIdentityAttributesFactory();

	void identityAttributesFactory();

	void applicationIdentityAttributesFactory();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Accessors.
	 */
	String getName();

	void setName(String name);

	String getDescription();

	void setDescription(String description);

	String getApplicationOwner();

	void setApplicationOwner(String applicationOwner);

	void setUpFile(UploadedFile uploadedFile);

	UploadedFile getUpFile();

	/*
	 * Actions.
	 */
	String add();

	String removeApplication();

	String save();

	String view();
}
