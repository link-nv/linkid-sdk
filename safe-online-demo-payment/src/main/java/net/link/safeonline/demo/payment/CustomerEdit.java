package net.link.safeonline.demo.payment;

import javax.ejb.Local;

@Local
public interface CustomerEdit extends AbstractPaymentDataClient {

	String persist();

}
