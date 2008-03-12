/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.node;

import javax.ejb.Local;

import org.apache.myfaces.trinidad.model.UploadedFile;

@Local
public interface Node {

	/*
	 * Factory
	 */
	void nodeListFactory();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Accessors.
	 */
	String getName();

	void setName(String name);

	String getHostname();

	void setHostname(String hostname);

	int getPort();

	void setPort(int port);

	int getSslPort();

	void setSslPort(int sslPort);

	void setAuthnUpFile(UploadedFile uploadedFile);

	UploadedFile getAuthnUpFile();

	void setSigningUpFile(UploadedFile uploadedFile);

	UploadedFile getSigningUpFile();

	/*
	 * Actions.
	 */
	String add();

	String remove();

	String save();

	String view();

	String edit();
}
