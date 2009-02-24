/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ctrl.error;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.cert.CertificateEncodingException;

import javax.faces.application.FacesMessage;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.management.relation.RoleNotFoundException;

import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.authentication.exception.AlreadySubscribedException;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeProviderNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.DeviceClassDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceClassNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePropertyNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.EndpointReferenceNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeProviderException;
import net.link.safeonline.authentication.exception.ExistingDeviceDescriptionException;
import net.link.safeonline.authentication.exception.ExistingDeviceException;
import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.DeviceRegistrationException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.helpdesk.exception.HelpdeskContextNotFoundException;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.exception.TrustPointNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.GenericJDBCException;
import org.jboss.seam.faces.FacesMessages;


/**
 * Seam Error Message interceptor. This interceptor will catch exceptions and try to construct a proper error message from them.
 * 
 * The interceptor can be used on method or class level. It also has a default set of Exception-Error message pairs. It will first look at a
 * possible annotation on the method, then the class and if still not found use this default set.
 * 
 * @ErrorHandling(
 * @Error(exceptionClass = PermissionDeniedException.class, messageId = "errorUserMayNotUnsubscribeFrom") })
 * 
 * @author wvdhaute
 * 
 */
public class ErrorMessageInterceptor {

    private static final Log LOG           = LogFactory.getLog(ErrorMessageInterceptor.class);

    private ErrorHandle[]    defaultErrors = { new ErrorHandle(AlreadySubscribedException.class, "errorAlreadySubscribed"),
            new ErrorHandle(ApplicationNotFoundException.class, "errorApplicationNotFound"),
            new ErrorHandle(ApplicationOwnerNotFoundException.class, "errorApplicationOwnerNotFound"),
            new ErrorHandle(ApplicationIdentityNotFoundException.class, "errorApplicationIdentityNotFound"),
            new ErrorHandle(AttributeNotFoundException.class, "errorAttributeNotFound"),
            new ErrorHandle(AttributeProviderNotFoundException.class, "errorAttributeProviderNotFound"),
            new ErrorHandle(AttributeTypeDescriptionNotFoundException.class, "errorAttributeTypeDescriptionNotFound"),
            new ErrorHandle(AttributeTypeNotFoundException.class, "errorAttributeTypeNotFound"),
            new ErrorHandle(AttributeUnavailableException.class, "errorAttributeUnavailable"),
            new ErrorHandle(AuditContextNotFoundException.class, "errorAuditContextNotFound"),
            new ErrorHandle(CertificateEncodingException.class, "errorX509Encoding"),
            new ErrorHandle(DeviceClassDescriptionNotFoundException.class, "errorDeviceClassDescriptionNotFound"),
            new ErrorHandle(DeviceClassNotFoundException.class, "errorDeviceClassNotFound"),
            new ErrorHandle(DeviceDescriptionNotFoundException.class, "errorDeviceDescriptionNotFound"),
            new ErrorHandle(DeviceDisabledException.class, "errorDeviceDisabled"),
            new ErrorHandle(DeviceNotFoundException.class, "errorDeviceNotFound"),
            new ErrorHandle(DevicePropertyNotFoundException.class, "errorDevicePropertyNotFound"),
            new ErrorHandle(DeviceRegistrationNotFoundException.class, "errorDeviceRegistrationNotFound"),
            new ErrorHandle(EmptyDevicePolicyException.class, "errorEmptyDevicePolicy"),
            new ErrorHandle(EndpointReferenceNotFoundException.class, "errorConsumerNotFound"),
            new ErrorHandle(ExistingAttributeProviderException.class, "errorAttributeProviderAlreadyExists"),
            new ErrorHandle(ExistingDeviceException.class, "errorDeviceAlreadyExists"),
            new ErrorHandle(ExistingDeviceDescriptionException.class, "errorDeviceDescriptionAlreadyExists"),
            new ErrorHandle(GenericJDBCException.class, "errorDataType"), new ErrorHandle(IOException.class, "errorIO"),
            new ErrorHandle(HelpdeskContextNotFoundException.class, "errorHelpdeskLogNotFound"),
            new ErrorHandle(MessageHandlerNotFoundException.class, "errorMessage"),
            new ErrorHandle(DeviceAuthenticationException.class, "mobileAuthenticationFailed"),
            new ErrorHandle(MobileException.class, "mobileCommunicationFailed"),
            new ErrorHandle(DeviceRegistrationException.class, "mobileRegistrationFailed"),
            new ErrorHandle(NodeNotFoundException.class, "errorNodeNotFound"),
            new ErrorHandle(PermissionDeniedException.class, "errorPermissionDenied"),
            new ErrorHandle(RoleNotFoundException.class, "errorRoleNotFound"),
            new ErrorHandle(SubjectNotFoundException.class, "errorSubjectNotFound"),
            new ErrorHandle(SubscriptionNotFoundException.class, "errorSubscriptionNotFound"),
            new ErrorHandle(TrustDomainNotFoundException.class, "errorTrustDomainNotFound"),
            new ErrorHandle(TrustPointNotFoundException.class, "errorTrustPointNotFound") };


    @AroundInvoke
    public Object invoke(InvocationContext invocationContext)
            throws Exception {

        try {
            return invocationContext.proceed();
        } catch (Exception e) {
            handleException(e, invocationContext);
        }
        return null;
    }

    private void handleException(Exception e, InvocationContext invocationContext) {

        // try the method error handling annotation
        Method method = invocationContext.getMethod();
        ErrorHandling methodErrorHandling = method.getAnnotation(ErrorHandling.class);

        if (null != methodErrorHandling) {
            if (handleError(methodErrorHandling, e))
                return;
        }

        // try the possible class error handling annotation
        Object target = invocationContext.getTarget();
        ErrorHandling classErrorHandling = target.getClass().getAnnotation(ErrorHandling.class);
        if (null != classErrorHandling) {
            if (handleError(classErrorHandling, e))
                return;
        }

        // try the default error set
        if (handleError(e))
            return;

        // unknown error
        FacesMessages.instance().addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorMessage");
        LOG.error("Unhandled exception: " + e.getClass().getName(), e);
        return;
    }

    private boolean handleError(ErrorHandling errorHandling, Exception e) {

        for (Error error : errorHandling.value()) {
            if (e.getClass().equals(error.exceptionClass())) {
                if (error.fieldId().equals(Error.NOT_SPECIFIED)) {
                    FacesMessages.instance().addFromResourceBundle(FacesMessage.SEVERITY_ERROR, error.messageId());
                } else {
                    FacesMessages.instance()
                                 .addToControlFromResourceBundle(error.fieldId(), FacesMessage.SEVERITY_ERROR, error.messageId());
                }
                return true;
            }
        }
        return false;
    }

    private boolean handleError(Exception e) {

        for (ErrorHandle error : defaultErrors) {
            if (e.getClass().equals(error.exceptionClass)) {
                if (null == error.fieldId) {
                    FacesMessages.instance().addFromResourceBundle(FacesMessage.SEVERITY_ERROR, error.messageId);
                } else {
                    FacesMessages.instance().addToControlFromResourceBundle(error.fieldId, FacesMessage.SEVERITY_ERROR, error.messageId);
                }
                return true;
            }
        }
        return false;
    }
}
