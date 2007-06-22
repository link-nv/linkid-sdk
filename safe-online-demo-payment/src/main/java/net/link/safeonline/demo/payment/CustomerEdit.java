package net.link.safeonline.demo.payment;

import javax.ejb.Local;

import net.link.safeonline.demo.payment.AbstractPaymentDataClient;

@Local
public interface CustomerEdit extends AbstractPaymentDataClient {

	String persist();

}
