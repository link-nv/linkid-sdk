/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;

import org.junit.Test;


/**
 * <h2>{@link AccountServiceTest}<br>
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
public class AccountServiceTest extends AbstractBankServiceTest {

    @EJB
    private InitializationService initializationService;

    @EJB
    private UserService           userService;

    @EJB
    private AccountService        accountService;


    /**
     * {@inheritDoc}
     */
    @Override
    public void setup() throws Exception {

        super.setup();

        this.initializationService.buildEntities();
    }

    @Test
    public void testCreateAccount() {

        // Test data.
        String testAccountName = "testAccount";

        // Create test account.
        BankUserEntity digipassUser = this.userService.getBankUser(InitializationService.digipassUser_BankId);
        this.accountService.createAccount(digipassUser, testAccountName);

        // Verify && account created successfully.
        List<String> sampleAccountNames = new LinkedList<String>();
        for (BankAccountEntity account : this.userService.getAccounts(digipassUser)) {
            sampleAccountNames.add(account.getName());
        }

        assertTrue(String.format("account not found: test: %s - sample: %s", testAccountName, sampleAccountNames), //
                sampleAccountNames.contains(testAccountName));
    }
}
