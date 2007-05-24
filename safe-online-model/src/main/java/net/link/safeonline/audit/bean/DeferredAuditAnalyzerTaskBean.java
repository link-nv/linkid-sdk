/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.bean;

import java.util.Date;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

import net.link.safeonline.Task;

/**
 * Deferred Audit Analyzer implemented as a Task.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@Local(Task.class)
@LocalBinding(jndiBinding = Task.JNDI_PREFIX + "/"
		+ "DeferredAuditAnalyzerTaskBean")
public class DeferredAuditAnalyzerTaskBean implements Task {

	private static final Log LOG = LogFactory
			.getLog(DeferredAuditAnalyzerTaskBean.class);

	public String getName() {
		return "Deferred Audit Analyzer";
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void perform() {
		Date now = new Date();
		LOG.debug("Analyzing Audit: " + now);
		// TODO: implement me, with feed to a Notifier.
	}
}
