/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.otpoversms;

import java.net.ConnectException;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import net.link.safeonline.otpoversms.ws.SmsServiceFactory;
import sis.mobile.SmsPortType;
import sis.mobile.SmsPortTypeSendSmsGenericFaultFaultMessage;
import sis.mobile.SmsService;

import com.sun.xml.ws.client.ClientTransportException;


public class SmsClientImpl implements SmsClient {

    private final SmsPortType smsPort;


    /**
     * Main constructor.
     * 
     * @param location
     */
    public SmsClientImpl(String location) {

        SmsService smsService = SmsServiceFactory.newInstance();
        smsPort = smsService.getSmsPort();

        BindingProvider bindingProvider = (BindingProvider) smsPort;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location);
    }

    /**
     * {@inheritDoc}
     */
    public void sendSms(String to, String message)
            throws ConnectException {

        try {
            smsPort.sendSms(to, message);
        } catch (ClientTransportException e) {
            throw new ConnectException(e.getMessage());
        } catch (SmsPortTypeSendSmsGenericFaultFaultMessage e) {
            throw new ConnectException(e.getMessage());
        } catch (WebServiceException e) {
            throw new ConnectException(e.getMessage());
        }

    }
}
