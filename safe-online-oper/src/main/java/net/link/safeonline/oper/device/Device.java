/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.device;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

import org.apache.myfaces.trinidad.model.UploadedFile;

@Local
public interface Device {

	/*
	 * Actions
	 */
	String view();

	String add();

	String remove();

	String edit();

	String save();

	/*
	 * Accessors
	 */
	String getName();

	void setName(String name);

	String getDeviceClass();

	void setDeviceClass(String deviceClass);

	String getAuthenticationURL();

	void setAuthenticationURL(String authenticationURL);

	String getRegistrationURL();

	void setRegistrationURL(String registrationURL);

	String getNewAccountRegistrationURL();

	void setNewAccountRegistrationURL(String newAccountRegistrationURL);

	String getRemovalURL();

	void setRemovalURL(String removalURL);

	UploadedFile getCertificate();

	void setCertificate(UploadedFile certificate);

	/*
	 * Factories
	 */
	void deviceListFactory();

	List<SelectItem> deviceClassesFactory();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

}
