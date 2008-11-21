/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.performance;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.ejb.Remote;


/**
 * Interface for performance service.
 * 
 * @author fcorneli
 * 
 */
@Remote
public interface PerformanceServiceRemote {

    public static final String JNDI_BINDING = "SafeOnline/PerformanceServiceBean/remote";


    PrivateKey getPrivateKey();

    X509Certificate getCertificate();
}