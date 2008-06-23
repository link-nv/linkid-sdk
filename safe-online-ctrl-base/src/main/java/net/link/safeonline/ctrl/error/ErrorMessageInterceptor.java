/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ctrl.error;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.faces.application.FacesMessage;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.management.AttributeNotFoundException;

import net.link.safeonline.authentication.exception.AlreadySubscribedException;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;

import org.jboss.seam.faces.FacesMessages;

/**
 * Seam Error Message interceptor. This interceptor will catch exceptions and
 * try to construct a proper error message from them.
 * 
 * The interceptor can be used on method or class level. It also has a default
 * set of Exception-Error message pairs. It will first look at a possible
 * annotation on the method, then the class and if still not found use this
 * default set.
 * 
 * @ErrorHandling( {
 * @Error(exceptionClass = PermissionDeniedException.class, messageId =
 *                       "errorUserMayNotUnsubscribeFrom") })
 * 
 * @author wvdhaute
 * 
 */
public class ErrorMessageInterceptor {

	private ErrorHandle[] defaultErrors = {
			new ErrorHandle(AlreadySubscribedException.class,
					"errorAlreadySubscribed"),
			new ErrorHandle(ApplicationNotFoundException.class,
					"errorApplicationNotFound"),
			new ErrorHandle(ApplicationIdentityNotFoundException.class,
					"errorApplicationIdentityNotFound"),
			new ErrorHandle(AttributeNotFoundException.class,
					"errorAttributeNotFound"),
			new ErrorHandle(AttributeTypeNotFoundException.class,
					"errorAttributeTypeNotFound"),
			new ErrorHandle(DeviceNotFoundException.class,
					"errorDeviceNotFound"),
			new ErrorHandle(EmptyDevicePolicyException.class,
					"errorEmptyDevicePolicy"),
			new ErrorHandle(IOException.class, "errorIO"),
			new ErrorHandle(PermissionDeniedException.class,
					"errorPermissionDenied"),
			new ErrorHandle(MessageHandlerNotFoundException.class,
					"errorMessage"),
			new ErrorHandle(SubjectNotFoundException.class,
					"errorSubjectNotFound"),
			new ErrorHandle(SubscriptionNotFoundException.class,
					"errorSubscriptionNotFound") };

	@AroundInvoke
	public Object invoke(InvocationContext invocationContext) throws Exception {
		try {
			return invocationContext.proceed();
		} catch (Exception e) {
			handleException(e, invocationContext);
		}
		return null;
	}

	private void handleException(Exception e,
			InvocationContext invocationContext) {
		// try the method error handling annotation
		Method method = invocationContext.getMethod();
		ErrorHandling methodErrorHandling = method
				.getAnnotation(ErrorHandling.class);

		if (null != methodErrorHandling) {
			if (handleError(methodErrorHandling, e)) {
				return;
			}
		}

		// try the possible class error handling annotation
		Object target = invocationContext.getTarget();
		ErrorHandling classErrorHandling = target.getClass().getAnnotation(
				ErrorHandling.class);
		if (null != classErrorHandling) {
			if (handleError(classErrorHandling, e)) {
				return;
			}
		}

		// try the default error set
		if (handleError(e)) {
			return;
		}

		// unknown error
		FacesMessages.instance().addFromResourceBundle(
				FacesMessage.SEVERITY_ERROR, "errorMessage");
		return;
	}

	private boolean handleError(ErrorHandling errorHandling, Exception e) {
		for (Error error : errorHandling.value()) {
			if (e.getClass().equals(error.exceptionClass())) {
				if (error.fieldId().equals(Error.NOT_SPECIFIED)) {
					FacesMessages.instance().addFromResourceBundle(
							FacesMessage.SEVERITY_ERROR, error.messageId());
				} else {
					FacesMessages.instance().addToControlFromResourceBundle(
							error.fieldId(), FacesMessage.SEVERITY_ERROR,
							error.messageId());
				}
				return true;
			}
		}
		return false;
	}

	private boolean handleError(Exception e) {
		for (ErrorHandle error : this.defaultErrors) {
			if (e.getClass().equals(error.exceptionClass)) {
				if (null == error.fieldId) {
					FacesMessages.instance().addFromResourceBundle(
							FacesMessage.SEVERITY_ERROR, error.messageId);
				} else {
					FacesMessages.instance().addToControlFromResourceBundle(
							error.fieldId, FacesMessage.SEVERITY_ERROR,
							error.messageId);
				}
				return true;
			}
		}
		return false;
	}
}
