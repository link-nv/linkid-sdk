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
import net.link.safeonline.demo.mandate.MandateConstants;
import net.link.safeonline.demo.mandate.MandateSearch;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.Attribute;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.log.Log;


@Stateful
@Name("mandateSearch")
@LocalBinding(jndiBinding = MandateSearch.JNDI_BINDING)
@SecurityDomain(MandateConstants.SECURITY_DOMAIN)
public class MandateSearchBean extends AbstractMandateDataClientBean implements MandateSearch {

    @Logger
    private Log       log;

    @SuppressWarnings("unused")
    @DataModel
    private Mandate[] mandates;

    @DataModelSelection
    private Mandate   selectedMandate;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private String    mandateUser;

    private String    name;


    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @RolesAllowed(MandateConstants.ADMIN_ROLE)
    public String search() {

        log.debug("search for #0", name);

        NameIdentifierMappingClient mappingClient = getNameIdentifierMappingClient();
        String userId;
        try {
            userId = mappingClient.getUserId(name);
        } catch (SubjectNotFoundException e) {
            facesMessages.addToControl("name", "subject not found");
            return null;
        } catch (RequestDeniedException e) {
            facesMessages.add("request denied");
            return null;
        } catch (WSClientTransportException e) {
            facesMessages.add("connection failed");
            return null;
        }

        DataClient dataClient = getDataClient();
        Attribute<Mandate[]> mandateAttribute;
        try {
            log.debug("get attribute value for user: " + userId);
            mandateAttribute = dataClient.getAttributeValue(userId, DemoConstants.MANDATE_ATTRIBUTE_NAME, Mandate[].class);
        } catch (WSClientTransportException e) {
            facesMessages.add("connection error: " + e.getMessage());
            return null;
        } catch (RequestDeniedException e) {
            facesMessages.add("request denied");
            return null;
        } catch (SubjectNotFoundException e) {
            facesMessages.addToControl("name", "subject not found");
            return null;
        }

        if (null != mandateAttribute) {
            mandates = mandateAttribute.getValue();
        } else {
            mandates = new Mandate[] {};
        }

        mandateUser = name;

        return "success";
    }

    @RolesAllowed(MandateConstants.ADMIN_ROLE)
    public String removeMandate() {

        log.debug("remove mandate : " + selectedMandate);

        NameIdentifierMappingClient mappingClient = getNameIdentifierMappingClient();
        String userId;
        try {
            userId = mappingClient.getUserId(mandateUser);
        } catch (SubjectNotFoundException e) {
            facesMessages.addToControl("name", "subject not found");
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
            dataClient.removeAttribute(userId, DemoConstants.MANDATE_ATTRIBUTE_NAME, selectedMandate.getAttributeId());
        } catch (WSClientTransportException e) {
            facesMessages.add("connection error: " + e.getMessage());
            return null;
        }
        return "success";
    }

}
