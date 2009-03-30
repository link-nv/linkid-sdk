/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.beid.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.AlreadyRegisteredException;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.PkiExpiredException;
import net.link.safeonline.authentication.exception.PkiInvalidException;
import net.link.safeonline.authentication.exception.PkiNotYetValidException;
import net.link.safeonline.authentication.exception.PkiRevokedException;
import net.link.safeonline.authentication.exception.PkiSuspendedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationStatement;
import net.link.safeonline.device.backend.CredentialManager;
import net.link.safeonline.model.beid.BeIdDeviceService;
import net.link.safeonline.model.beid.BeIdDeviceServiceRemote;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;


@Stateless
@LocalBinding(jndiBinding = BeIdDeviceService.JNDI_BINDING)
@RemoteBinding(jndiBinding = BeIdDeviceServiceRemote.JNDI_BINDING)
public class BeIdDeviceServiceBean implements BeIdDeviceService, BeIdDeviceServiceRemote {

    private final static Log  LOG = LogFactory.getLog(BeIdDeviceServiceBean.class);

    @EJB(mappedName = CredentialManager.JNDI_BINDING)
    private CredentialManager credentialManager;

    @EJB(mappedName = BeIdPkiProviderBean.JNDI_BINDING)
    private PkiProvider       beIdPkiProvider;


    public String authenticate(String sessionId, String applicationId, AuthenticationStatement authenticationStatement)
            throws TrustDomainNotFoundException, SubjectNotFoundException, ArgumentIntegrityException, PkiRevokedException,
            PkiSuspendedException, PkiExpiredException, PkiNotYetValidException, PkiInvalidException, DeviceDisabledException,
            DeviceRegistrationNotFoundException {

        LOG.debug("authenticate: sessionId=" + sessionId + " applicaitonId=" + applicationId);
        return credentialManager.authenticate(sessionId, applicationId, authenticationStatement);
    }

    public void register(String sessionId, String nodeName, String userId, String operation, byte[] identityStatementData)
            throws TrustDomainNotFoundException, PermissionDeniedException, ArgumentIntegrityException, AlreadyRegisteredException,
            PkiRevokedException, PkiSuspendedException, PkiExpiredException, PkiNotYetValidException, PkiInvalidException,
            NodeNotFoundException {

        LOG.debug("register: sessionId=" + sessionId + "nodeName=" + nodeName + " userId=" + userId + " operation=" + operation);
        credentialManager.mergeIdentityStatement(sessionId, nodeName, userId, operation, identityStatementData);
    }

    public void enable(String sessionId, String userId, String operation, byte[] identityStatementData)
            throws TrustDomainNotFoundException, SubjectNotFoundException, PermissionDeniedException, ArgumentIntegrityException,
            PkiRevokedException, PkiSuspendedException, PkiExpiredException, PkiNotYetValidException, PkiInvalidException,
            DeviceRegistrationNotFoundException {

        LOG.debug("enable: sessionId=" + sessionId + " userId=" + userId + " operation=" + operation);
        credentialManager.enable(sessionId, userId, operation, identityStatementData);
    }

    public void disable(String userId, String attributeId)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        LOG.debug("disable: userId=" + userId + " attributeId=" + attributeId);
        beIdPkiProvider.disable(userId, attributeId);
    }

    public void remove(String userId, String attributeId)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        LOG.debug("remove: userId=" + userId + " attributeId=" + attributeId);
        beIdPkiProvider.remove(userId, attributeId);

    }

}
