/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service.bean;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.demo.wicket.service.AbstractWicketServiceBean;


/**
 * <h2>{@link AbstractBankServiceBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 23, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class AbstractBankServiceBean extends AbstractWicketServiceBean {

    @PersistenceContext(unitName = "DemoBankEntityManager")
    protected EntityManager em = defaultEntityManager;


    /**
     * {@inheritDoc}
     */
    @Override
    protected EntityManager getEntityManager() {

        return this.em;
    }
}
