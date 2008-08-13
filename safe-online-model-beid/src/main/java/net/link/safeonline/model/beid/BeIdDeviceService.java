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
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.PkiExpiredException;
import net.link.safeonline.authentication.exception.PkiInvalidException;
import net.link.safeonline.authentication.exception.PkiNotYetValidException;
import net.link.safeonline.authentication.exception.PkiRevokedException;
import net.link.safeonline.authentication.exception.PkiSuspendedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.bean.AuthenticationStatement;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;


@Local
public interface BeIdDeviceService {

    String authenticate(String sessionId, String applicationId, AuthenticationStatement authenticationStatement)
            throws ArgumentIntegrityException, TrustDomainNotFoundException, SubjectNotFoundException,
            PkiRevokedException, PkiSuspendedException, PkiExpiredException, PkiNotYetValidException,
            PkiInvalidException;

    void register(String sessionId, String deviceUserId, String operation, byte[] identityStatementData)
            throws PermissionDeniedException, ArgumentIntegrityException, TrustDomainNotFoundException,
            AttributeTypeNotFoundException, DeviceNotFoundException, AttributeNotFoundException,
            AlreadyRegisteredException, PkiRevokedException, PkiSuspendedException, PkiExpiredException,
            PkiNotYetValidException, PkiInvalidException;

    void remove(String sessionId, String deviceUserId, String operation, byte[] identityStatementData)
            throws TrustDomainNotFoundException, PermissionDeniedException, ArgumentIntegrityException,
            AttributeTypeNotFoundException, SubjectNotFoundException, DeviceNotFoundException, PkiRevokedException,
            PkiSuspendedException, PkiExpiredException, PkiNotYetValidException, PkiInvalidException;
}
