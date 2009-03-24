/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.bean;

import javax.ejb.Stateless;

import net.link.safeonline.demo.prescription.PrescriptionAuthentication;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;


@Stateless
@Name("prescriptionAuthentication")
@LocalBinding(jndiBinding = PrescriptionAuthentication.JNDI_BINDING)
public class PrescriptionAuthenticationBean implements PrescriptionAuthentication {

    @Logger
    private Log log;


    public String authenticate(String target) {

        log.debug("authentication for target: #0", target);
        String result = SafeOnlineLoginUtils.login(target, true);
        return result;
    }
}
