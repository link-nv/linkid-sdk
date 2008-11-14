/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.model.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.Task;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.dao.CachedOcspResponseDAO;
import net.link.safeonline.pkix.dao.TrustDomainDAO;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Task.class)
@LocalBinding(jndiBinding = CachedOcspResponseCleanerTaskBean.JNDI_BINDING)
public class CachedOcspResponseCleanerTaskBean implements Task {

    public static final String    JNDI_BINDING = Task.JNDI_PREFIX + "CachedOcspResponseCleanerTaskBean/local";

    private static final String   name         = "Ocsp response cache cleaner";

    @EJB(mappedName = TrustDomainDAO.JNDI_BINDING)
    private TrustDomainDAO        trustDomainDAO;

    @EJB(mappedName = CachedOcspResponseDAO.JNDI_BINDING)
    private CachedOcspResponseDAO cachedOcspResponseDAO;


    public CachedOcspResponseCleanerTaskBean() {

        // empty
    }

    public String getName() {

        return name;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform() {

        List<TrustDomainEntity> trustDomains = this.trustDomainDAO.listTrustDomains();
        for (TrustDomainEntity trustDomain : trustDomains) {
            this.cachedOcspResponseDAO.clearOcspCachePerTrustDomain(trustDomain);
        }

    }

}
