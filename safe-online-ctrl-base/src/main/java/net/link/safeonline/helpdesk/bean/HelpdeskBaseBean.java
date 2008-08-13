/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.bean;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import net.link.safeonline.helpdesk.HelpdeskBase;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.HelpdeskContact;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.log.Log;


public class HelpdeskBaseBean implements HelpdeskBase {

    @Logger
    private Log             LOG;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private Long            id;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private String          location;

    @EJB
    private HelpdeskContact contact;


    @PostConstruct
    public void init() {

    }

    public String createTicket() {

        if (null == this.location) {

            Map<?, ?> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            this.location = (String) params.get("location");
            if (null == this.location) {
                this.location = FacesContext.getCurrentInstance().getExternalContext().getRequestServletPath();
            }
        }
        this.LOG.debug("helpdesk location: " + this.location);

        this.LOG.debug("create helpdesk ticket");
        this.id = HelpdeskLogger.persistContext(this.location, (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false));
        return "create-ticket";
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

    public String getDummy() {

        Map<?, ?> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        this.location = (String) params.get("location");
        this.LOG.debug("helpdesk location: " + this.location);

        return "";
    }
}
