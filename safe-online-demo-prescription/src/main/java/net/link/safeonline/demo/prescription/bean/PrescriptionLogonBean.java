/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.bean;

import java.security.Principal;

import javax.ejb.Stateful;
import javax.faces.context.FacesContext;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import net.link.safeonline.demo.prescription.PrescriptionConstants;
import net.link.safeonline.demo.prescription.PrescriptionLogon;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.log.Log;
import org.jboss.security.SimplePrincipal;


@Stateful
@Name("prescriptionLogon")
@LocalBinding(jndiBinding = PrescriptionLogon.JNDI_BINDING)
public class PrescriptionLogonBean extends AbstractPrescriptionDataClientBean implements PrescriptionLogon {

    @Logger
    private Log log;

    @In
    Context     sessionContext;


    public String login() {

        this.log.debug("login");
        String result = SafeOnlineLoginUtils.login("login");
        return result;
    }

    public String logout() {

        this.log.debug("logout");
        String userId = (String) this.sessionContext.get(LoginManager.USERID_SESSION_ATTRIBUTE);
        SafeOnlineLoginUtils.logout(userId, "main.seam");
        return "success";
    }

    public String getUsername() {

        String userId = (String) this.sessionContext.get(LoginManager.USERID_SESSION_ATTRIBUTE);
        return getUsername(userId);
    }

    private void activateRole(String role) {

        String userId = (String) this.sessionContext.get(LoginManager.USERID_SESSION_ATTRIBUTE);
        flushCredentialCache(userId, PrescriptionConstants.SECURITY_DOMAIN);

        FacesContext context = FacesContext.getCurrentInstance();
        this.log.debug("set role: " + role);
        context.getExternalContext().getSessionMap().put("role", role);
    }

    private void flushCredentialCache(String login, String securityDomain) {

        this.log.debug("flush credential cache for " + login + " on security domain " + securityDomain);
        Principal user = new SimplePrincipal(login);
        ObjectName jaasMgr;
        try {
            jaasMgr = new ObjectName("jboss.security:service=JaasSecurityManager");
        } catch (MalformedObjectNameException e) {
            String msg = "ObjectName error: " + e.getMessage();
            this.log.error(msg);
            throw new RuntimeException(msg, e);
        } catch (NullPointerException e) {
            throw new RuntimeException("NPE: " + e.getMessage(), e);
        }
        Object[] params = { securityDomain, user };
        String[] signature = { String.class.getName(), Principal.class.getName() };
        MBeanServer server = MBeanServerLocator.locateJBoss();
        try {
            server.invoke(jaasMgr, "flushAuthenticationCache", params, signature);
        } catch (InstanceNotFoundException e) {
            String msg = "instance not found: " + e.getMessage();
            this.log.error(msg);
            throw new RuntimeException(msg, e);
        } catch (MBeanException e) {
            String msg = "mbean error: " + e.getMessage();
            this.log.error(msg);
            throw new RuntimeException(msg, e);
        } catch (ReflectionException e) {
            String msg = "reflection error: " + e.getMessage();
            this.log.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    public String activateAdminRole() {

        activateRole(PrescriptionConstants.ADMIN_ROLE);
        return "admin";
    }

    public String activateCareProviderRole() {

        activateRole(PrescriptionConstants.CARE_PROVIDER_ROLE);
        return "careProvider";
    }

    public String activatePharmacistRole() {

        activateRole(PrescriptionConstants.PHARMACIST_ROLE);
        return "pharmacist";
    }
}
