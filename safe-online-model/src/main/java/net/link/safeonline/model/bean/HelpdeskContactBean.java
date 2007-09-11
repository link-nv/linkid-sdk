/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.model.HelpdeskContact;

@Stateless
@Interceptors(ConfigurationInterceptor.class)
@Configurable
public class HelpdeskContactBean implements HelpdeskContact {

	@Configurable(name = "Phone number", group = "Helpdesk contact information")
	private String phone = "911";

	@Configurable(name = "E-mail", group = "Helpdesk contact information")
	private String email = "help@lin-k.net";

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
