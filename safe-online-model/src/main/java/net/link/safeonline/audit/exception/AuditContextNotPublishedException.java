/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.exception;

import javax.ejb.ApplicationException;

import net.link.safeonline.authentication.exception.SafeOnlineException;

@ApplicationException(rollback = true)
public class AuditContextNotPublishedException extends SafeOnlineException {

	private static final long serialVersionUID = 1L;

	private Long auditContextId;

	private String errorCode;

	private String message;

	public AuditContextNotPublishedException(Long auditContextId,
			String message, String errorCode) {
		this.auditContextId = auditContextId;
		this.errorCode = errorCode;
		this.message = message;
	}

	public Long getAuditContextId() {
		return this.auditContextId;
	}

	@Override
	public String getErrorCode() {
		return this.errorCode;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

}
