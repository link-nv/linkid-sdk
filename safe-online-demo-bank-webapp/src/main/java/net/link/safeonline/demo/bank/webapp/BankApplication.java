package net.link.safeonline.demo.bank.webapp;

import net.link.safeonline.demo.wicket.tools.WicketUtil;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;


public class BankApplication extends WebApplication {


    @Override
    protected void init() {

        WicketUtil.addInjector(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Page<?>> getHomePage() {

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
