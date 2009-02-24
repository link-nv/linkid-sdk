/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi;

/**
 * <h2>{@link OSGIConstants}<br>
 * <sub>Contains some OSGI Constants</sub></h2>
 * 
 * <p>
 * This interface contains some OSGI Constants that need to be available outside s-o-model.
 * </p>
 * 
 * <p>
 * <i>Feb 18, 2009</i>
 * </p>
 * 
 * @author dhouthoo
 */
public interface OSGIConstants {

    public static final String SMS_SERVICE_GROUP_NAME = "SMS Service";
    public static final String SMS_SERVICE_IMPL_NAME  = "Implementations";


    public static enum OSGIServiceType {
        PLUGIN_SERVICE,
        SMS_SERVICE;
    }

}
