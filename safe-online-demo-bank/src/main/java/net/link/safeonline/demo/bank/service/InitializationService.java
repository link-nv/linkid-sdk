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

    public static final String JNDI_BINDING                = JNDI_PREFIX + "InitializationServiceBean/local";

    public String              digipassUser_BankId         = "pol";
    public String              digipassUser_Name           = "Pol Van Acker";
    public String[]            digipassUser_AccountNames   = { "Persoonlijk", "Kinderen", "Spaarrekening" };
    public String[]            digipassUser_AccountCodes   = { "543-3246784-43", "433-2532784-34", "412-3524758-61" };
    public double[]            digipassUser_AccountAmounts = { 972.54d, 468.40d, 24050.90d };


    public void buildEntities();
}
