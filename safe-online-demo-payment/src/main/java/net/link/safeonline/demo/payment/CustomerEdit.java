package net.link.safeonline.demo.payment;

import javax.ejb.Local;


@Local
public interface CustomerEdit extends AbstractPaymentDataClient {

    public static final String JNDI_BINDING = "SafeOnlinePaymentDemo/CustomerEditBean/local";


    String persist();

}
