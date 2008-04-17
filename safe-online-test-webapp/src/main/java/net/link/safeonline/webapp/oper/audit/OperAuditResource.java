/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.oper.audit;

import net.link.safeonline.webapp.oper.OperTemplate;

public class OperAuditResource extends OperTemplate {

	public static final String PAGE_NAME = SAFE_ONLINE_OPER_WEBAPP_PREFIX
			+ "/audit/audit-resource.seam";

	public OperAuditResource() {
		super(PAGE_NAME);
	}
}