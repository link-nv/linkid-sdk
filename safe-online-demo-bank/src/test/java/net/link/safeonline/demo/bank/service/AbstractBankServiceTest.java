/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankTransactionEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;
import net.link.safeonline.demo.bank.service.bean.AccountServiceBean;
import net.link.safeonline.demo.bank.service.bean.InitializationServiceBean;
import net.link.safeonline.demo.bank.service.bean.TransactionServiceBean;
import net.link.safeonline.demo.bank.service.bean.UserServiceBean;


/**
 * <h2>{@link AbstractBankServiceTest}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Oct 16, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class AbstractBankServiceTest extends AbstractServiceTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getEntities() {

        return new Class<?>[] { BankAccountEntity.class, BankTransactionEntity.class, BankUserEntity.class };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getServices() {

        return new Class<?>[] { AccountServiceBean.class, InitializationServiceBean.class,
                TransactionServiceBean.class, UserServiceBean.class };
    }
}
