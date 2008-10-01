/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service;

import javax.ejb.Local;


/**
 * <h2>{@link InitializationService}<br>
 * <sub>Service that executes after application deployment.</sub></h2>
 * 
 * <p>
 * Creates some initial dummy entities to fill up the database with user accounts and transactions.
 * </p>
 * 
 * <p>
 * <i>Jun 23, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface InitializationService extends BankService {

    public static final String       BINDING = JNDI_PREFIX + "InitializationServiceBean/local";

    public static final String[] bankIds = { "pol", "maarten", "dieter", "wim" };


    public void buildEntities();
}
