/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.beid;

import javax.ejb.Local;

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
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;


@Local
public interface BeIdDeviceService extends BeIdService {

    public static final String JNDI_BINDING = BeIdService.JNDI_PREFIX + "BeIdDeviceServiceBean/local";


    String authenticate(String sessionId, String applicationId, AuthenticationStatement authenticationStatement)
            throws TrustDomainNotFoundException, SubjectNotFoundException, ArgumentIntegrityException, PkiRevokedException,
            PkiSuspendedException, PkiExpiredException, PkiNotYetValidException, PkiInvalidException, DeviceDisabledException,
            DeviceRegistrationNotFoundException;

    void register(String sessionId, String nodeName, String userId, String operation, byte[] identityStatementData)
            throws TrustDomainNotFoundException, PermissionDeniedException, ArgumentIntegrityException, AlreadyRegisteredException,
            PkiRevokedException, PkiSuspendedException, PkiExpiredException, PkiNotYetValidException, PkiInvalidException,
            NodeNotFoundException;

    void enable(String sessionId, String userId, String operation, byte[] identityStatementData)
            throws TrustDomainNotFoundException, SubjectNotFoundException, PermissionDeniedException, ArgumentIntegrityException,
            PkiRevokedException, PkiSuspendedException, PkiExpiredException, PkiNotYetValidException, PkiInvalidException,
            DeviceRegistrationNotFoundException;

    void disable(String userId, String attributeId)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException;

    void remove(String userId, String attributeId)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException;
}
