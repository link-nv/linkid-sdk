/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.converters;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.webapp.BankSession;
import net.link.safeonline.demo.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.util.convert.IConverter;


/**
 * <h2>{@link BankAccountConverter}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * The converter creates a dummy {@link BankAccountEntity} from a string that describes a bank account. Attaching this
 * dummy should give us a valid entity.
 * </p>
 * 
 * <p>
 * The converter also describes {@link BankAccountEntity}s in a string form.
 * </p>
 * 
 * <p>
 * <i>Sep 30, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class BankAccountConverter implements IConverter<BankAccountEntity> {

    private static final long serialVersionUID = 1L;
    private static final Log  LOG              = LogFactory.getLog(BankAccountEntity.class);

    private final Pattern     parser           = Pattern.compile("([\\d-]*): (.*) \\[([^\\]]*)\\]");


    /**
     * {@inheritDoc}
     */
    public BankAccountEntity convertToObject(String value, Locale locale) {

        Matcher matcher = this.parser.matcher(value);
        if (matcher.matches()) {
            String code = matcher.group(1);
            String name = matcher.group(2);
            String amount = matcher.group(3);
            BankAccountEntity entity = new BankAccountEntity(null, name, code);
            try {
                entity.setAmount((Double) NumberFormat.getCurrencyInstance(BankSession.CURRENCY).parse(amount));
            } catch (ParseException e) {
                LOG.warn("Couldn't parse " + amount + " into a currency amount for bank account: " + code + " (" + name
                        + ")");
            }

            return entity;
        }

        throw new IllegalArgumentException("Value '" + value + "' cannot be converted into a BankAccountEntity.");
    }

    /**
     * {@inheritDoc}
     */
    public String convertToString(BankAccountEntity value, Locale locale) {

        return String.format("%s: %s [%s]", value.getCode(), value.getName(), WicketUtil.format(BankSession.CURRENCY,
                value.getAmount()));
    }
}
