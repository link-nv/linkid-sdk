/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.lawyer.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateful;

import net.link.safeonline.demo.lawyer.LawyerConstants;
import net.link.safeonline.demo.lawyer.LawyerSearch;
import net.link.safeonline.demo.lawyer.LawyerStatus;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.log.Log;


@Stateful
@Name("lawyerSearch")
@LocalBinding(jndiBinding = LawyerSearch.JNDI_BINDING)
@SecurityDomain(LawyerConstants.SECURITY_DOMAIN)
public class LawyerSearchBean extends AbstractLawyerDataClientBean implements LawyerSearch {

    @Logger
    private Log          log;

    @In("name")
    @Out(scope = ScopeType.SESSION)
    private String       name;

    @SuppressWarnings("unused")
    @Out(value = "lawyerEditableStatus", required = false, scope = ScopeType.SESSION)
    private LawyerStatus lawyerStatus;


    @RolesAllowed(LawyerConstants.ADMIN_ROLE)
    public String search() {

        this.log.debug("search: " + this.name);
        NameIdentifierMappingClient nameClient = getNameIdentifierMappingClient();
        String userId;
        try {
            userId = nameClient.getUserId(this.name);
        } catch (SubjectNotFoundException e) {
            this.facesMessages.add("subject not found");
            return null;
        } catch (RequestDeniedException e) {
            this.facesMessages.add("request denied");
            return null;
        } catch (WSClientTransportException e) {
            this.facesMessages.add("connection failed");
            return null;
        }
        LawyerStatus currentLawyerStatus = getLawyerStatus(userId);
        if (null == currentLawyerStatus)
            return null;
        this.lawyerStatus = currentLawyerStatus;
        return "success";
    }
}
