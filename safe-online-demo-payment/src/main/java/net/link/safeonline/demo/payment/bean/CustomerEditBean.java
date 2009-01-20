/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.bean;

import javax.ejb.Stateful;

import net.link.safeonline.demo.payment.CustomerEdit;
import net.link.safeonline.demo.payment.CustomerStatus;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.log.Log;


@Stateful
@Name("customerEdit")
@LocalBinding(jndiBinding = CustomerEdit.JNDI_BINDING)
public class CustomerEditBean extends AbstractPaymentDataClientBean implements CustomerEdit {

    @Logger
    private Log            log;

    @In("name")
    @Out("name")
    private String         name;

    @SuppressWarnings("unused")
    @In("customerEditableStatus")
    @Out("customerEditableStatus")
    private CustomerStatus customerStatus;


    public String persist() {

        log.debug("---------------------------------------- save #0 -----------------------------", name);

        try {
            createOrUpdateAttribute(DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, Boolean.valueOf(customerStatus.isJunior()));
        } catch (WSClientTransportException e) {
            facesMessages.add("connection error");
            return null;
        } catch (RequestDeniedException e) {
            facesMessages.add("request denied");
            return null;
        } catch (SubjectNotFoundException e) {
            facesMessages.add("subject not found: " + name);
            return null;
        } catch (AttributeNotFoundException e) {
            facesMessages.add("attribute not found");
            return null;
        }
        return "success";
    }

    private void createOrUpdateAttribute(String attributeName, Object attributeValue)
            throws WSClientTransportException, RequestDeniedException, SubjectNotFoundException, AttributeNotFoundException {

        DataClient dataClient = getDataClient();
        if (null == dataClient.getAttributeValue(customerStatus.getUserId(), attributeName, attributeValue.getClass())) {
            log.debug("create attribute #0 for #1", attributeName, name);
            dataClient.createAttribute(customerStatus.getUserId(), attributeName, attributeValue);
        } else {
            log.debug("set attribute #0 for #1", attributeName, name);
            dataClient.setAttributeValue(customerStatus.getUserId(), attributeName, attributeValue);
        }
    }
}
