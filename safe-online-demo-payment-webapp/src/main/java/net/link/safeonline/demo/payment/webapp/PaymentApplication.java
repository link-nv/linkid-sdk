package net.link.safeonline.demo.payment.webapp;


public class PaymentApplication extends WebApplication {

    public static final String ADMIN_MOUNTPOINT   = "admin";
    public static final String SERVICE_MOUNTPOINT = "service";


    @Override
    protected void init() {

        // Java EE annotations injector.
        WicketUtil.addInjector(this);

        mountBookmarkablePage(SERVICE_MOUNTPOINT, NewServicePage.class);
        mountBookmarkablePage(ADMIN_MOUNTPOINT, AdminPage.class);
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

        return new PaymentSession(request);
    }
}
