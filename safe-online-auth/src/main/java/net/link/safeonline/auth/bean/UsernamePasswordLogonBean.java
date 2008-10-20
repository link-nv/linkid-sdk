/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import javax.annotation.PostConstruct;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.UsernamePasswordLogon;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;


@Stateful
@Name("usernamePasswordLogon")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX + "UsernamePasswordLogonBean/local")
public class UsernamePasswordLogonBean extends AbstractLoginBean implements UsernamePasswordLogon {

    private static final Log      LOG = LogFactory.getLog(UsernamePasswordLogonBean.class);

    private String                loginname;

    private String                password;

    @In
    private AuthenticationService authenticationService;


    @Remove
    @Destroy
    public void destroyCallback() {

        this.loginname = null;
        this.password = null;
    }

    public String getPassword() {

        return this.password;
    }

    public String getUsername() {

        return this.loginname;
    }

    @PostConstruct
    public void init() {

    }

    public String login() {

        LOG.debug("login: " + this.loginname);
        HelpdeskLogger.add("login: " + this.loginname, LogLevelType.INFO);
        super.clearUserId();

        try {
            boolean authenticated = this.authenticationService.authenticate(this.loginname, this.password);
            if (false == authenticated) {
                /*
                 * The abort will be correctly handled by the authentication service manager. That way we allow the user
                 * to retry the initial authentication step.
                 */
                this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
                HelpdeskLogger.add("login failed: " + this.loginname, LogLevelType.ERROR);
                return null;
            }
        } catch (SubjectNotFoundException e) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
            HelpdeskLogger.add("login: subject not found for " + this.loginname, LogLevelType.ERROR);
            return null;
        } catch (DeviceNotFoundException e) {
            /*
             * Important here not to explicitly communicate that the password device was not configured.
             */
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
            HelpdeskLogger.add("password device not configured", LogLevelType.ERROR);
            return null;
        } catch (DeviceDisabledException e) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorDeviceDisabled");
            HelpdeskLogger.add("login: password device disabled for subject " + this.loginname, LogLevelType.ERROR);
            return null;
        }

        super.login(this.loginname, SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, this.authenticationService
                .getSsoCookie());

        return null;
    }


    public static final String AUTH_SERVICE_ATTRIBUTE = "authenticationService";


    public void setPassword(String password) {

        this.password = password;
    }

    public void setUsername(String username) {

        this.loginname = username;
    }
}
