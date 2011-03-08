/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.auth.protocol.openid;

import com.google.common.collect.AbstractIterator;
import java.util.Iterator;
import org.joda.time.DateTime;
import org.openid4java.message.*;


/**
 * <h2>{@link AuthenticatedDevicesMessage}</h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Sep 4, 2009</i>
 * </p>
 *
 * @author wvdhaute
 */
public class AuthenticatedDevicesMessage implements MessageExtension, MessageExtensionFactory, Iterable<String> {

    /**
     * The Authenticate Devices Type URI.
     */
    public static final String LINKID_AUTH_DEVICES_NS = "http://linkid.be/srv/auth/devices/1.0";

    private static final String TIME_PARAM_PREFIX = "time.";
    private static final String DEVICE_PARAM_PREFIX = "device.";

    private ParameterList parameters;

    private int count = 0;


    public AuthenticatedDevicesMessage() {

        parameters = new ParameterList();
    }

    public AuthenticatedDevicesMessage(ParameterList parameterList) {

        parameters = parameterList;
    }

    public void addAuthentication(DateTime time, String deviceName) {

        parameters.set( new Parameter( TIME_PARAM_PREFIX + count, time.toString() ) );
        parameters.set( new Parameter( DEVICE_PARAM_PREFIX + count, deviceName ) );

        count++;
    }

    /**
     * {@inheritDoc}
     */
    public String getTypeUri() {

        return LINKID_AUTH_DEVICES_NS;
    }

    /**
     * {@inheritDoc}
     */
    public ParameterList getParameters() {

        return parameters;
    }

    /**
     * {@inheritDoc}
     */
    public void setParameters(ParameterList params) {

        parameters = params;
    }

    /**
     * {@inheritDoc}
     */
    public boolean providesIdentifier() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean signRequired() {

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public MessageExtension getExtension(ParameterList parameterList, boolean isRequest)
            throws MessageException {

        return new AuthenticatedDevicesMessage( parameterList );
    }

    public Iterator<String> iterator() {

        return new AbstractIterator<String>() {

            @SuppressWarnings({ "unchecked" })
            private Iterator<Parameter> source = parameters.getParameters().iterator();

            @Override
            protected String computeNext() {

                while (source.hasNext()) {
                    Parameter param = source.next();
                    String paramName = param.getKey();
                    String paramValue = param.getValue();

                    if (paramName.startsWith( DEVICE_PARAM_PREFIX ))
                        return paramValue;
                }

                return endOfData();
            }
        };
    }
}
