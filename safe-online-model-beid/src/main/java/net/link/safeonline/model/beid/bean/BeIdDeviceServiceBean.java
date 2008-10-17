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
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.PkiExpiredException;
import net.link.safeonline.authentication.exception.PkiInvalidException;
import net.link.safeonline.authentication.exception.PkiNotYetValidException;
import net.link.safeonline.authentication.exception.PkiRevokedException;
import net.link.safeonline.authentication.exception.PkiSuspendedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.bean.AuthenticationStatement;
import net.link.safeonline.device.backend.CredentialManager;
import net.link.safeonline.model.beid.BeIdDeviceService;
import net.link.safeonline.model.beid.BeIdDeviceServiceRemote;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class BeIdDeviceServiceBean implements BeIdDeviceService, BeIdDeviceServiceRemote {

    private final static Log  LOG = LogFactory.getLog(BeIdDeviceServiceBean.class);

    @EJB
    private CredentialManager credentialManager;


    public String authenticate(String sessionId, String applicationId, AuthenticationStatement authenticationStatement)
            throws ArgumentIntegrityException, TrustDomainNotFoundException, SubjectNotFoundException,
            PkiRevokedException, PkiSuspendedException, PkiExpiredException, PkiNotYetValidException,
            PkiInvalidException, DeviceNotFoundException, DeviceDisabledException {

        LOG.debug("authenticate: sessionId=" + sessionId + " applicaitonId=" + applicationId);
        return this.credentialManager.authenticate(sessionId, applicationId, authenticationStatement);
    }

    public void register(String sessionId, String userId, String operation, byte[] identityStatementData)
            throws PermissionDeniedException, ArgumentIntegrityException, AttributeTypeNotFoundException,
            TrustDomainNotFoundException, DeviceNotFoundException, AttributeNotFoundException,
            AlreadyRegisteredException, PkiRevokedException, PkiSuspendedException, PkiExpiredException,
            PkiNotYetValidException, PkiInvalidException {

        LOG.debug("register: sessionId=" + sessionId + " userId=" + userId + " operation=" + operation);
        this.credentialManager.mergeIdentityStatement(sessionId, userId, operation, identityStatementData);
    }

    public void remove(String sessionId, String userId, String operation, byte[] identityStatementData)
            throws TrustDomainNotFoundException, PermissionDeniedException, ArgumentIntegrityException,
            AttributeTypeNotFoundException, SubjectNotFoundException, DeviceNotFoundException, PkiRevokedException,
            PkiSuspendedException, PkiExpiredException, PkiNotYetValidException, PkiInvalidException {

        LOG.debug("remove: sessionId=" + sessionId + " userId=" + userId + " operation=" + operation);
        this.credentialManager.removeIdentity(sessionId, userId, operation, identityStatementData);
    }
}
