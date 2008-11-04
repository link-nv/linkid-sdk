/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankTransactionEntity;

import org.junit.Test;


/**
 * <h2>{@link TransactionServiceTest}<br>
 * <sub>Unit tests for {@link TransactionService}.</sub></h2>
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
public class TransactionServiceTest extends AbstractBankServiceTest {

    @EJB
    private InitializationService initializationService;

    @EJB
    private AccountService        accountService;

    @EJB
    private TransactionService    transactionService;


    /**
     * {@inheritDoc}
     */
    @Override
    public void setup()
            throws Exception {

        super.setup();

        this.initializationService.buildEntities();
    }

    /**
     * @see TransactionService#createTransaction(String, BankAccountEntity, String, double)
     */
    @Test
    public void testCreateTransaction() {

        // Test data.
        String testDescription = "This is a test transaction.";
        Double testAmount = 1000d;
        String testSourceCode = InitializationService.digipassUser_AccountCodes[0];
        String testTargetCode = InitializationService.digipassUser_AccountCodes[1];

        // Create test transaction.
        BankAccountEntity testTransactionSourceAccount = this.accountService.getAccount(testSourceCode);
        this.transactionService.createTransaction(testDescription, testTransactionSourceAccount, testTargetCode, testAmount);

        // Verify && transaction created successfully.
        List<BankTransactionEntity> transactions = this.transactionService.getAllTransactions(testTransactionSourceAccount);
        assertTrue(transactions.size() == 1);
        BankTransactionEntity transaction = transactions.get(0);

        // - Collect sample data.
        String sampleDescription = transaction.getDescription();
        Double sampleAmount = transaction.getAmount();
        String sampleSourceCode = transaction.getSource().getCode();
        String sampleTargetCode = transaction.getTarget();

        // - Test sample data against our original test data.
        assertTrue(String.format("description mismatch: test: %s - sample: %s", testDescription, sampleDescription), //
                testDescription.equals(sampleDescription));
        assertTrue(String.format("amount mismatch: test: %s - sample: %s", testAmount, sampleAmount), //
                testAmount.equals(sampleAmount));
        assertTrue(String.format("source code mismatch: test: %s - sample: %s", testSourceCode, sampleSourceCode), //
                testSourceCode.equals(sampleSourceCode));
        assertTrue(String.format("target code mismatch: test: %s - sample: %s", testTargetCode, sampleTargetCode), //
                testTargetCode.equals(sampleTargetCode));
    }
}
