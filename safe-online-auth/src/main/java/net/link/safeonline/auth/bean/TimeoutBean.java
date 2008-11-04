/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

import net.link.safeonline.auth.Timeout;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;


@Stateful
@Name("timeout")
@LocalBinding(jndiBinding = Timeout.JNDI_BINDING)
@Interceptors(ErrorMessageInterceptor.class)
public class TimeoutBean extends AbstractExitBean implements Timeout {

    @Remove
    @Destroy
    public void destroyCallback() {

    }

    public String getApplicationUrl() {

        return findApplicationUrl();
    }
}
