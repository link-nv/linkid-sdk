package net.link.safeonline.demo.bank.webapp;

import net.link.safeonline.demo.bank.converters.BankAccountConverter;
import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.convert.ConverterLocator;


public class BankApplication extends WebApplication {

    @Override
    protected void init() {

        // Java EE annotations injector.
        WicketUtil.addInjector(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IConverterLocator newConverterLocator() {

        ConverterLocator converterLocator = new ConverterLocator();
        converterLocator.set(BankAccountEntity.class, new BankAccountConverter());

        return converterLocator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Page> getHomePage() {

        return LoginPage.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session newSession(Request request, Response response) {

        return new BankSession(request);
    }
}
