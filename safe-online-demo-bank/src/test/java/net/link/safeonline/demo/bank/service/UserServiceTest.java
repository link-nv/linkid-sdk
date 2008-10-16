/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;

import org.junit.Test;


/**
 * <h2>{@link UserServiceTest}<br>
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
public class UserServiceTest extends AbstractBankServiceTest {

    @EJB
    private InitializationService initializationService;

    @EJB
    private UserService           userService;


    /**
     * {@inheritDoc}
     */
    @Override
    public void setup() throws Exception {

        super.setup();

        this.initializationService.buildEntities();
    }

    @Test
    public void testGetAccounts() {

        // Test data.
        String testBankId = InitializationService.digipassUser_BankId;
        List<String> testAccountCodes = Arrays.asList(InitializationService.digipassUser_AccountCodes);

        // Create test transaction.
        BankUserEntity digipassUser = this.userService.getBankUser(testBankId);

        // Verify && registered accounts present.
        List<BankAccountEntity> accounts = this.userService.getAccounts(digipassUser);

        // - Collect sample data.
        List<String> sampleAccountCodes = new LinkedList<String>();
        for (BankAccountEntity account : accounts) {
            String sampleBankId = account.getUser().getBankId();
            
            assertTrue(String.format("account owner mismatch: test: %s - sample: %s", testBankId, sampleBankId), //
                    sampleBankId.equals(testBankId));
            
            sampleAccountCodes.add(account.getCode());
        }

        // - Test sample data against our original test data.
        assertTrue(String.format("accounts mismatch: test: %s - sample: %s", testAccountCodes, sampleAccountCodes), //
                testAccountCodes.size() == sampleAccountCodes.size()
                        && testAccountCodes.containsAll(sampleAccountCodes));
    }
}
