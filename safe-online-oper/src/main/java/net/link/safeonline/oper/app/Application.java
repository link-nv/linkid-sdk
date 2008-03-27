/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.app;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

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

	List<SelectItem> availableApplicationOwnersFactory();

	List<SelectItem> appliactionIdScopeFactory();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Accessors.
	 */
	String getName();

	void setName(String name);

	String getFriendlyName();

	void setFriendlyName(String friendlyName);

	String getDescription();

	void setDescription(String description);

	String getApplicationUrl();

	void setApplicationUrl(String applicationUrl);

	byte[] getApplicationLogo();

	void setApplicationLogo(byte[] applicationLogo);

	UploadedFile getApplicationLogoFile();

	void setApplicationLogoFile(UploadedFile applicationLogoFile);

	String getApplicationColor();

	void setApplicationColor(String applicationColor);

	String getApplicationOwner();

	void setApplicationOwner(String applicationOwner);

	void setUpFile(UploadedFile uploadedFile);

	UploadedFile getUpFile();

	boolean isIdmapping();

	void setIdmapping(boolean idmapping);

	String getUsageAgreement();

	void setApplicationIdScope(String applicationIdScope);

	String getApplicationIdScope();

	boolean isSkipMessageIntegrityCheck();

	void setSkipMessageIntegrityCheck(boolean skipMessageIntegrityCheck);

	/*
	 * Actions.
	 */
	String add();

	String removeApplication();

	String save();

	String view();

	String edit();
}
