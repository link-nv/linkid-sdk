package net.link.safeonline.demo.payment;

import javax.ejb.Local;


import net.link.safeonline.SafeOnlineService;

@Local
public interface CustomerEdit extends SafeOnlineService, AbstractPaymentDataClient {

    String persist();

}
