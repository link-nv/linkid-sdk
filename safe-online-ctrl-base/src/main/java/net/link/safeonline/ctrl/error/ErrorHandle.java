/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.ctrl.error;

/**
 * Used by {@link ErrorMessageInterceptor} for some default exception-error message handlers.
 * 
 * @author wvdhaute
 * 
 */
public class ErrorHandle {

    public Class<? extends Exception> exceptionClass;

    public String                     messageId;

    public String                     fieldId = null;


    public ErrorHandle(Class<? extends Exception> exceptionClass, String messageId) {

        this.exceptionClass = exceptionClass;
        this.messageId = messageId;
    }

    public ErrorHandle(Class<? extends Exception> exceptionClass, String messageId, String fieldId) {

        this.exceptionClass = exceptionClass;
        this.messageId = messageId;
        this.fieldId = fieldId;
    }

}
