/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateful;

import net.link.safeonline.demo.mandate.Mandate;
import net.link.safeonline.demo.mandate.MandateAdd;
import net.link.safeonline.demo.mandate.MandateConstants;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;


@Stateful
@Name("mandateAdd")
@LocalBinding(jndiBinding = MandateAdd.JNDI_BINDING)
@SecurityDomain(MandateConstants.SECURITY_DOMAIN)
public class MandateAddBean extends AbstractMandateDataClientBean implements MandateAdd {

    @In
    private String             mandateUser;

    @Logger
    private Log                log;

    public static final String NEW_MANDATE = "newMandate";

    @In(value = NEW_MANDATE, required = false)
    private Mandate            newMandate;


    @RolesAllowed(MandateConstants.ADMIN_ROLE)
    public String add() {

        log.debug("add new mandate for user #0", mandateUser);

        NameIdentifierMappingClient mappingClient = getMappingClient();
        String mandateUserId;
        try {
            mandateUserId = mappingClient.getUserId(mandateUser);
        } catch (SubjectNotFoundException e) {
            facesMessages.add("subject not found");
            return null;
        } catch (RequestDeniedException e) {
            facesMessages.add("request denied");
            return null;
        } catch (WSClientTransportException e) {
            facesMessages.add("connection failed");
            return null;
        }

        DataClient dataClient = getDataClient();
        try {
            dataClient.createAttribute(mandateUserId, DemoConstants.MANDATE_ATTRIBUTE_NAME, newMandate);
        } catch (WSClientTransportException e) {
            facesMessages.add("connection error");
            return null;
        }

        return "success";
    }

    @Factory(NEW_MANDATE)
    public Mandate newMandateFactory() {

        return new Mandate();
    }
}
