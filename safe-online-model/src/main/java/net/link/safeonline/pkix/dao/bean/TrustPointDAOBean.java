/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.dao.bean;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.entity.pkix.TrustPointPK;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.pkix.dao.TrustPointDAO;
import net.link.safeonline.pkix.exception.TrustPointNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = TrustPointDAO.JNDI_BINDING)
public class TrustPointDAOBean implements TrustPointDAO {

    private static final Log                LOG = LogFactory.getLog(TrustPointDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                   entityManager;

    private TrustPointEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, TrustPointEntity.QueryInterface.class);
    }

    public void addTrustPoint(TrustDomainEntity trustDomain, X509Certificate certificate) {

        LOG.debug("add trust point to domain: " + trustDomain.getName() + " with subject " + certificate.getSubjectX500Principal());
        TrustPointEntity trustPoint = new TrustPointEntity(trustDomain, certificate);
        entityManager.persist(trustPoint);
    }

    public List<TrustPointEntity> listTrustPoints(TrustDomainEntity trustDomain) {

        LOG.debug("get trust points for domain: " + trustDomain.getName());
        List<TrustPointEntity> trustPoints = queryObject.listTrustPoints(trustDomain);
        return trustPoints;
    }

    public void removeTrustPoint(TrustPointEntity trustPoint) {

        LOG.debug("remove trust point: " + trustPoint);
        entityManager.remove(trustPoint);
    }

    public TrustPointEntity findTrustPoint(TrustDomainEntity trustDomain, X509Certificate certificate) {

        TrustPointPK pk = new TrustPointPK(trustDomain, certificate);
        TrustPointEntity trustPoint = entityManager.find(TrustPointEntity.class, pk);
        return trustPoint;
    }

    public TrustPointEntity getTrustPoint(TrustPointPK pk)
            throws TrustPointNotFoundException {

        TrustPointEntity trustPoint = entityManager.find(TrustPointEntity.class, pk);
        if (null == trustPoint)
            throw new TrustPointNotFoundException();
        return trustPoint;
    }

    public List<TrustPointEntity> listTrustPoints(String certificateSubject) {

        LOG.debug("get trust points with certificate subject: " + certificateSubject);
        return queryObject.listTrustPoints(certificateSubject);
    }
}
