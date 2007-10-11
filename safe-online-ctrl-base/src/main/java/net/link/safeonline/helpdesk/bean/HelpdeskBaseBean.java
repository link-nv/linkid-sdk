/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.bean;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remove;

import net.link.safeonline.helpdesk.HelpdeskBase;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.HelpdeskContact;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;

public class HelpdeskBaseBean implements HelpdeskBase {

	@Logger
	private Log LOG;

	Long id;

	@EJB
	private HelpdeskContact contact;

	@PostConstruct
	public void init() {
		this.LOG.debug("persisting volatile log");
		this.id = HelpdeskLogger.persistContext();
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		this.LOG.debug("destroy: #0", this);
	}

	public Long getId() {
		return this.id;
	}

	public String getEmail() {
		return this.contact.getEmail();
	}

	public String getPhone() {
		return this.contact.getPhone();
	}

}