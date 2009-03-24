/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateful;

import net.link.safeonline.demo.prescription.PrescriptionConstants;
import net.link.safeonline.demo.prescription.PrescriptionEdit;
import net.link.safeonline.demo.prescription.UserStatus;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.log.Log;


@Stateful
@Name("prescriptionEdit")
@LocalBinding(jndiBinding = PrescriptionEdit.JNDI_BINDING)
@SecurityDomain(PrescriptionConstants.SECURITY_DOMAIN)
public class PrescriptionEditBean extends AbstractPrescriptionDataClientBean implements PrescriptionEdit {

    @Logger
    private Log        log;

    @In("userStatus")
    @Out("userStatus")
    private UserStatus userStatus;


    @RolesAllowed(PrescriptionConstants.ADMIN_ROLE)
    public String persist() {

        try {
            createOrUpdateAttribute(DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME, Boolean.valueOf(userStatus.isAdmin()), Boolean.class);
            createOrUpdateAttribute(DemoConstants.PRESCRIPTION_CARE_PROVIDER_ATTRIBUTE_NAME, Boolean.valueOf(userStatus.isCareProvider()),
                    Boolean.class);
            createOrUpdateAttribute(DemoConstants.PRESCRIPTION_PHARMACIST_ATTRIBUTE_NAME, Boolean.valueOf(userStatus.isPharmacist()),
                    Boolean.class);
        } catch (WSClientTransportException e) {
            facesMessages.add("connection error");
            return null;
        } catch (RequestDeniedException e) {
            facesMessages.add("request denied");
            return null;
        } catch (SubjectNotFoundException e) {
            facesMessages.add("subject not found: " + userStatus.getName());
            return null;
        } catch (AttributeNotFoundException e) {
            facesMessages.add("attribute not found");
            return null;
        }
        return "success";
    }

    private void createOrUpdateAttribute(String attributeName, Object attributeValue, Class<?> expectedClass)
            throws WSClientTransportException, RequestDeniedException, SubjectNotFoundException, AttributeNotFoundException {

        DataClient dataClient = getDataClient();
        String userId = userStatus.getUserId();
        if (null == dataClient.getAttributeValue(userId, attributeName, expectedClass)) {
            log.debug("create attribute #0 for #1", attributeName, userId);
            dataClient.createAttribute(userId, attributeName, attributeValue);
        } else {
            log.debug("set attribute #0 for #1", attributeName, userId);
            dataClient.setAttributeValue(userId, attributeName, attributeValue);
        }
    }
}
