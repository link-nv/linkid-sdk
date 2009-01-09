/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate.bean;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

import net.link.safeonline.demo.mandate.Mandate;
import net.link.safeonline.demo.mandate.MandateConstants;
import net.link.safeonline.demo.mandate.MandateView;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.Attribute;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.log.Log;


@Stateful
@Name("mandateView")
@LocalBinding(jndiBinding = MandateView.JNDI_BINDING)
@SecurityDomain(MandateConstants.SECURITY_DOMAIN)
public class MandateViewBean extends AbstractMandateDataClientBean implements MandateView {

    @SuppressWarnings("unused")
    @DataModel
    private Mandate[]          mandates;

    @Logger
    private Log                log;

    @In(required = false)
    private String             mandateUser;

    @Resource
    private SessionContext     context;

    public static final String USER_MANDATES = "userMandates";

    @SuppressWarnings("unused")
    @DataModel(USER_MANDATES)
    private Mandate[]          userMandates;


    @Factory("mandates")
    @RolesAllowed(MandateConstants.ADMIN_ROLE)
    public void mandatesFactory() {

        log.debug("mandates factory for user: #0", mandateUser);

        NameIdentifierMappingClient mappingClient = super.getMappingClient();
        String mandateUserId;
        try {
            mandateUserId = mappingClient.getUserId(mandateUser);
        } catch (SubjectNotFoundException e) {
            facesMessages.addToControl("name", "subject not found");
            mandates = new Mandate[] {};
            return;
        } catch (RequestDeniedException e) {
            facesMessages.add("request denied");
            mandates = new Mandate[] {};
            return;
        } catch (WSClientTransportException e) {
            facesMessages.add("connection failed");
            mandates = new Mandate[] {};
            return;
        }

        DataClient dataClient = getDataClient();
        Attribute<Mandate[]> mandateAttribute = null;
        try {
            mandateAttribute = dataClient.getAttributeValue(mandateUserId, DemoConstants.MANDATE_ATTRIBUTE_NAME, Mandate[].class);
        } catch (WSClientTransportException e) {
            facesMessages.add("connection error: " + e.getMessage());
        } catch (RequestDeniedException e) {
            facesMessages.add("request denied");
        } catch (SubjectNotFoundException e) {
            facesMessages.addToControl("name", "subject not found");
        }

        if (null != mandateAttribute) {
            mandates = mandateAttribute.getValue();
        } else {
            mandates = new Mandate[] {};
        }
    }

    @RolesAllowed(MandateConstants.USER_ROLE)
    @Factory(USER_MANDATES)
    public void userMandatesFactory() {

        Principal callerPrincipal = context.getCallerPrincipal();
        String username = callerPrincipal.getName();
        log.debug("user mandates factory for user #0", username);

        DataClient dataClient = getDataClient();
        Attribute<Mandate[]> mandateAttribute = null;
        try {
            mandateAttribute = dataClient.getAttributeValue(username, DemoConstants.MANDATE_ATTRIBUTE_NAME, Mandate[].class);
        } catch (WSClientTransportException e) {
            facesMessages.add("connection error: " + e.getMessage());
        } catch (RequestDeniedException e) {
            facesMessages.add("request denied");
        } catch (SubjectNotFoundException e) {
            facesMessages.addToControl("name", "subject not found");
        }

        if (null != mandateAttribute) {
            userMandates = mandateAttribute.getValue();
        } else {
            userMandates = new Mandate[] {};
        }
    }
}
